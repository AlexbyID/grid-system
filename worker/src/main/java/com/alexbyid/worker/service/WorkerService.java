package com.alexbyid.worker.service;

import com.alexbyid.worker.system.state.WorkerState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.alexbyid.worker.web.dto.response.ApiResponse;
import com.alexbyid.worker.web.dto.request.TaskRequest;
import com.alexbyid.worker.enums.WorkerStatusEnum;
import com.alexbyid.worker.utils.reflexion.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;
import java.io.IOException;


@Service
@Slf4j
public class WorkerService {
    @Autowired
    private HttpSenderService httpSenderService;
    @Autowired
    private SolverService solverService;
    @Autowired
    private WorkerState workerState;
    @Value("${upload-path}")
    private String UPLOAD_PATH;
    @Value("${temp-path}")
    private String TEMP_PATH;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Async
    public void solve(TaskRequest taskDTO) {
        workerState.setWorkerStatusEnum(WorkerStatusEnum.WORKING);
        ObjectNode result = (ObjectNode) solverService.solve(taskDTO);

        ObjectNode finalResult = JsonNodeFactory.instance.objectNode();
        finalResult.set("result", result);
        finalResult.set("task", mapper.valueToTree(taskDTO));

        Mono<ApiResponse> responseMono = httpSenderService.sendPostRequest(taskDTO.getCallbackUrl(), finalResult, ApiResponse.class);

        responseMono.subscribe(
                response -> log.info("Status code: {}. Response: {}", response.getStatusCode(), response.getMessage()),
                error -> log.error("Error: {}", error.getMessage())
        );

        log.info("Task {}:{} completed successfully. Result: {}", taskDTO.getStart(), taskDTO.getCount(), result);
        workerState.setWorkerStatusEnum(WorkerStatusEnum.FREE);
    }

    public void reset() {
        workerState.setWorkerStatusEnum(WorkerStatusEnum.UNINITIALIZED);
        solverService.setZipPath(null);
        solverService.setSolveMethod(null);
        try {
            ReflectionUtils.classLoader.close();
            FileSystemUtils.deleteRecursively(Paths.get(UPLOAD_PATH));
            FileSystemUtils.deleteRecursively(Paths.get(TEMP_PATH));
        } catch (IOException e) {
            log.error("An error occurred while deleting the directory {}", e.getMessage());
        }
        log.info("The worker was reset successfully");
    }
}
