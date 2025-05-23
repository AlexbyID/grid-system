package com.alexbyid.distributor.service;

import com.alexbyid.distributor.distributor.TaskManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alexbyid.distributor.web.response.ApiResponse;
import com.alexbyid.distributor.system.dto.WorkerDTO;
import com.alexbyid.distributor.enums.WorkerStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class DistributorService {

    @Autowired
    private HttpSenderService httpSenderService;
    @Autowired
    private TaskManager taskManager;

    @Value("${worker-init-files.jar-file-path}")
    private String JAR_FILE_PATH;
    @Value("${worker-init-files.manifest-path}")
    private String MANIFEST_PATH;
    @Value("${uploads.matrix-path}")
    private String MATRIX_PATH;

    @Value("${manager.get-workers-endpoint}")
    private String MANAGER_GET_WORKERS_ENDPOINT;
    @Value("${upload-path}")
    private String UPLOAD_PATH;
    @Getter
    @Setter
    private String matrixName = null;

    private static final long PROCESS_INTERVAL_MS = 10_000;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Scheduled(fixedRate = PROCESS_INTERVAL_MS)
    public void processTask() {
        if (matrixName == null) return;

        httpSenderService.sendGetRequest(MANAGER_GET_WORKERS_ENDPOINT, ApiResponse.class)
                .flatMap(response -> {
                    Object data = response.getData();
                    if (data == null) {
                        return Mono.just(List.<WorkerDTO>of());
                    }
                    log.info("Successfully get workers - {}", response.getData());
                    return Mono.fromCallable(() -> {
                        String jsonString = mapper.writeValueAsString(data);
                        return mapper.readValue(jsonString, new TypeReference<List<WorkerDTO>>() {});
                    }).subscribeOn(Schedulers.boundedElastic());
                })
                .flatMapMany(Flux::fromIterable)
                .delayElements(Duration.ofMillis(100))
                .flatMap(worker -> {
                    if (worker.getWorkerStatus().equals(WorkerStatusEnum.UNINITIALIZED)) {
                        return initWorker(worker, matrixName).thenReturn(worker);
                    }
                    else if (worker.getWorkerStatus().equals(WorkerStatusEnum.FREE)) {
                        return sendTask(worker).thenReturn(worker);
                    }
                    return Mono.just(worker);
                })
                .collectList()
                .doOnSuccess(taskManager::taskRedistribution)
                .subscribe(
                        null,
                        error -> log.error("Error in processTask: {}",  error.getMessage())
                );
    }

    private Mono<Void> initWorker(WorkerDTO workerDTO, String matrixName) {
        return Mono.fromCallable(() -> {
                    MultipartBodyBuilder builder = new MultipartBodyBuilder();
                    builder.part("jarFile", new FileSystemResource(Paths.get(JAR_FILE_PATH)));
                    builder.part("jsonData", new FileSystemResource(Paths.get(MANIFEST_PATH)));
                    builder.part("jsonMatrix", new FileSystemResource(Paths.get(UPLOAD_PATH, matrixName)));

                    return builder.build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(multipartBody -> httpSenderService.sendMultipartPostRequest(
                        workerDTO.initAddress(),
                        multipartBody,
                        ApiResponse.class
                ))
                .doOnNext(response -> log.info("Status code: {}. Response: {}", response.getStatusCode(), response.getMessage()))
                .doOnError(error -> log.error("Error: {}", error.getMessage()))
                .then();
    }

    private Mono<Void> sendTask(WorkerDTO workerDTO) {
        return Mono.fromCallable(() -> taskManager.getTask())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(task -> {
                    if (task == null) {
                        log.info("No tasks available, skipping send operation");
                        return Mono.empty();
                    }

                    task.setWorker(workerDTO.fullAddress());
                    return httpSenderService.sendPostRequest(
                            workerDTO.solveAddress(),
                            task,
                            ApiResponse.class
                    );
                })
                .doOnNext(response ->
                        log.info("Status code: {}. Response: {}", response.getStatusCode(), response.getMessage()))
                .doOnError(error ->
                        log.error("Failed to send task: {}", error.getMessage()))
                .then();
    }

    public Mono<Void> resetAllWorkers() {
        return httpSenderService.sendGetRequest(MANAGER_GET_WORKERS_ENDPOINT, ApiResponse.class)
                .flatMapMany(response -> {
                    Object data = response.getData();
                    if (data == null) {
                        return Flux.empty();
                    }
                    return Mono.fromCallable(() -> {
                                String jsonString = mapper.writeValueAsString(data);
                                return mapper.readValue(jsonString, new TypeReference<List<WorkerDTO>>() {});
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMapMany(Flux::fromIterable);
                })
                .flatMap(worker -> httpSenderService.sendGetRequest(worker.resetAddress(), Void.class)
                        .onErrorResume(error -> {
                            log.error("Failed to reset worker {}: {}", worker.fullAddress(), error.getMessage());
                            return Mono.empty();
                        }))
                .then();
    }

    public void stopGlobalTask() {
        matrixName = null;
        taskManager.reset();
        resetAllWorkers()
                .subscribe(
                        null,
                        error -> log.error("Failed to reset workers", error),
                        () -> log.info("All workers reset successfully")
                );
        log.info("Global task was completed!");
    }
}
