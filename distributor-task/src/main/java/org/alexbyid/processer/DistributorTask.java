package org.alexbyid.processer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DistributorTask {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @End
    public static String calculateEndRange(String jsonData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonData);
            JsonNode matrixLength = jsonNode.get("matrixLength");
//            BigInteger asciiCharacters = BigInteger.valueOf(94);
            return matrixLength.asText();
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Process
    public static boolean processResult(String result) {
        try {
            JsonNode jsonNode = objectMapper.readTree(result);
            JsonNode matrixNode = jsonNode.get("solution");

            if (matrixNode != null && !matrixNode.isNull()) {
                String matrix = matrixNode.asText();

                Path matrixFile = Paths.get("matrix.txt");
                Files.writeString(matrixFile, matrix, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            return false;
        }
    }

}
