package com.alexbyid.distributor.bootstrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alexbyid.distributor.service.DistributorJarService;
import com.alexbyid.distributor.distributor.TaskManager;
import com.alexbyid.distributor.utils.reflection.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Paths;


@Component
@Slf4j
public class ApplicationStartup
        implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private TaskManager taskManager;
    @Autowired
    private DistributorJarService distributorJarService;
    @Value("${distributor-init-files.jar-file-path}")
    private String JAR_FILE_PATH;
    @Value("${distributor-init-files.manifest-path}")
    private String MANIFEST_PATH;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application started!");

        try {
            JsonNode manifestJson = mapper.readTree(Paths.get(MANIFEST_PATH).toFile());

            Method calculateEndMethod =
                    ReflectionUtils.getAnnotatedMethodsByName(
                            Paths.get(JAR_FILE_PATH), manifestJson.get("className").asText(), manifestJson.get("annotationEndName").asText())
                            .get(0);
            taskManager.setFinalEnd(
                    new BigInteger(
                            distributorJarService.executeCalculateEnd(
                            calculateEndMethod,
                            mapper.writeValueAsString(manifestJson.get("data"))).toString()
                    )
            );

            distributorJarService.setProcessResult(
                    ReflectionUtils.getAnnotatedMethodsByName(
                                    Paths.get(JAR_FILE_PATH), manifestJson.get("className").asText(), manifestJson.get("annotationProcessName").asText())
                            .get(0));

            log.info("Success init");

        } catch (Exception e) {
            log.error("An error occurred while initializing the distributor {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
