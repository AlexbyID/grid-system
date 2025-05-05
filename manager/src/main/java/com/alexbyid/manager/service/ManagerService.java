package com.alexbyid.manager.service;

import com.alexbyid.manager.web.dto.response.ApiResponse;
import com.alexbyid.manager.web.dto.request.WorkerDTO;
import com.alexbyid.manager.enums.WorkerStatusEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class ManagerService {

    @Getter
    private final Set<WorkerDTO> workerList = new HashSet<>();
    private static final long HEALTH_CHECK_INTERVAL_MS = 5_000;

    @Autowired
    private HttpSenderService httpSenderService;
    @Autowired
    private Executor executor;
    @Autowired
    private TaskScheduler taskScheduler;

    public void registerWorker(WorkerDTO worker){
        workerList.add(worker);
        log.info("Added new worker {}:{}. Remaining workers: {}", worker.getHost(), worker.getPort(), workerList.size());
    }

    public void leaveWorker(WorkerDTO worker) {
        workerList.remove(worker);
        log.info("Worker leaved {}:{}. Remaining workers: {}", worker.getHost(), worker.getPort(), workerList.size());
    }

    @Scheduled(fixedRate = HEALTH_CHECK_INTERVAL_MS)
    public void checkWorkersStatus() {
        Flux.fromIterable(workerList)
                .flatMap(worker ->
                        httpSenderService.sendGetRequest(worker.checkStateAddress(), ApiResponse.class)
                                .timeout(Duration.ofSeconds(3))
                                .map(apiResponse -> {
                                    worker.setWorkerStatus(
                                            WorkerStatusEnum.valueOf(apiResponse.getData().toString()));
                                    log.info("Successfully check - {}, {} {}", worker.fullAddress(), apiResponse.getMessage(), apiResponse.getData());
                                    return worker;
                                })
                                .onErrorResume(error -> {
                                    log.error("Worker {} check failed: {}", worker.fullAddress(), error.getMessage());
                                    leaveWorker(worker);
                                    return Mono.just(worker);
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .subscribe(
                        null,
                        error -> log.error("Critical error in worker check: {}", error.getMessage())
                );
    }

}
