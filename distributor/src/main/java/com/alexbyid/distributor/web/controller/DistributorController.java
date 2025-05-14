package com.alexbyid.distributor.web.controller;

import com.alexbyid.distributor.distributor.TaskManager;
import com.alexbyid.distributor.service.DistributorJarService;
import com.alexbyid.distributor.service.DistributorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.alexbyid.distributor.web.response.ApiResponse;
import com.alexbyid.distributor.system.dto.TaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
public class DistributorController {

    @Autowired
    private DistributorService distributorService;
    @Autowired
    private DistributorJarService distributorJarService;
    @Autowired
    private TaskManager taskManager;
    @Value("${upload-path}")
    private String UPLOAD_PATH;
    private static final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/start")
    public ResponseEntity<ApiResponse> startTask(@RequestParam("matrixName") String matrixName) {

        if (!isMatrixFileValid(matrixName)) {
            ApiResponse response = new ApiResponse("No such file exists", HttpStatus.NOT_FOUND.value(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        log.info("Received new task. MatrixName: {}", matrixName);
        distributorService.setMatrixName(matrixName);

        ApiResponse response = new ApiResponse("Task started", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public boolean isMatrixFileValid(String matrixName) {
        try {
            if (matrixName == null || matrixName.isEmpty()) {
                return false;
            }

            Path filePath = Paths.get(UPLOAD_PATH, matrixName);
            return Files.exists(filePath)
                    && Files.isRegularFile(filePath)
                    && matrixName.toLowerCase().endsWith(".json");
        } catch (Exception e) {
            log.error("Error checking Matrix data file {}: {}", matrixName, e.getMessage());
            return false;
        }
    }

    @PostMapping("/result")
    public ResponseEntity<ApiResponse> receiveResult(@RequestBody ObjectNode result) throws JsonProcessingException {
        if (distributorService.getMatrixName() == null) {
            ApiResponse response = new ApiResponse("Global task already completed", HttpStatus.OK.value(), null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        JsonNode resultNode = result.get("result");

        TaskDTO taskDTO = mapper.treeToValue(result.get("task"), TaskDTO.class);
        log.info("Our task??? - {}", taskDTO);
        taskManager.removeCompetedTask(taskDTO);

        boolean processResult = (boolean) distributorJarService.executeProcessResult(resultNode.toString());
        if (processResult ||
                taskManager.getFinalEnd().equals(new BigInteger(taskDTO.getCount()))) distributorService.stopGlobalTask();

        ApiResponse response = new ApiResponse("Result received successfully", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
