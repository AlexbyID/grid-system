package com.alexbyid.worker.web.controller;

import com.alexbyid.worker.service.SolverService;
import com.alexbyid.worker.service.WorkerService;
import com.alexbyid.worker.system.state.WorkerState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alexbyid.worker.web.dto.response.ApiResponse;
import com.alexbyid.worker.system.dto.ManifestDTO;
import com.alexbyid.worker.web.dto.request.TaskRequest;
import com.alexbyid.worker.enums.WorkerStatusEnum;
import com.alexbyid.worker.utils.reflexion.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
public class WorkerController {

    @Autowired
    private WorkerService workerService;
    @Autowired
    private SolverService solverService;
    @Autowired
    private WorkerState workerState;
    @Value("${upload-path}")
    private String UPLOAD_PATH;

    public static final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/init")
    public ResponseEntity<ApiResponse> init(@RequestPart("jarFile") Part jarFile,
                                            @RequestPart("archiveFile") Part archiveFile,
                                            @RequestPart("jsonData") Part jsonData,
                                            @RequestPart("jsonMatrix") Part jsonMatrix)
    {

        Path uploadDir = Paths.get(UPLOAD_PATH);

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path jarFilePath = uploadDir.resolve(jarFile.headers().getContentDisposition().getFilename());
            byte[] jarBytes = DataBufferUtils.join(jarFile.content())
                    .map(DataBuffer::asByteBuffer)
                    .block()
                    .array();
            Files.write(jarFilePath, jarBytes);

            Path archiveFilePath = uploadDir.resolve(archiveFile.headers().getContentDisposition().getFilename());
            byte[] archiveBytes = DataBufferUtils.join(archiveFile.content())
                    .map(DataBuffer::asByteBuffer)
                    .block()
                    .array();
            Files.write(archiveFilePath, archiveBytes);

            Path jsonFilePath = uploadDir.resolve(jsonData.headers().getContentDisposition().getFilename());
            byte[] jsonBytes = DataBufferUtils.join(jsonData.content())
                    .map(DataBuffer::asByteBuffer)
                    .block()
                    .array();
            Files.write(jsonFilePath, jsonBytes);

            Path jsonMatrixPath = uploadDir.resolve(jsonMatrix.headers().getContentDisposition().getFilename());
            byte[] jsonMatrixBytes = DataBufferUtils.join(jsonMatrix.content())
                    .map(DataBuffer::asByteBuffer)
                    .block()
                    .array();
            Files.write(jsonMatrixPath, jsonMatrixBytes);

            File jsonFile = jsonFilePath.toFile();
            ManifestDTO manifestDTO = mapper.readValue(jsonFile, ManifestDTO.class);

            solverService.setSolveMethod(ReflectionUtils.getAnnotatedMethodsByName(
                    jarFilePath,
                    manifestDTO.getClassName(),
                    manifestDTO.getAnnotationName()).get(0));
            solverService.setZipPath(archiveFilePath);
            File jsonMatrixFile = jsonMatrixPath.toFile();
            workerState.setWorkerStatusEnum(WorkerStatusEnum.FREE);
            log.info("Success init");

            ApiResponse response = new ApiResponse("Success init", HttpStatus.OK.value(), null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            ApiResponse response = new ApiResponse("Error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/solve")
    public ResponseEntity<ApiResponse> solve(@RequestBody TaskRequest taskDTO) {
        log.info("Received task: {}:{}", taskDTO.getStart(), taskDTO.getCount());
        workerService.solve(taskDTO);

        ApiResponse response = new ApiResponse("Task received", HttpStatus.ACCEPTED.value(), null);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/check-state")
    public ResponseEntity<ApiResponse> checkWorkerState() {
        WorkerStatusEnum workerStatusEnum = workerState.getWorkerStatusEnum();
        ApiResponse response = new ApiResponse("Worker state", HttpStatus.OK.value(), workerStatusEnum);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/reset")
    public ResponseEntity<ApiResponse> reset() {
        workerService.reset();

        ApiResponse response = new ApiResponse("Reset successfully", HttpStatus.OK.value(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
