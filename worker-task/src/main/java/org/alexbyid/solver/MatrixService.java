package org.alexbyid.solver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alexbyid.solver.dto.MatrixDTO;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MatrixService {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Solve
    public static ObjectNode start(Path matrixPath, String startNumberString, String endNumberString) throws IOException {

        BigInteger startNumber = new BigInteger(startNumberString);
        BigInteger endNumber = new BigInteger(endNumberString);

        MatrixDTO matrix = mapper.readValue(matrixPath.toFile(), MatrixDTO.class);

        LatinSquareSolver latinSquareSolver = new LatinSquareSolver(matrix);
        return latinSquareSolver.solveRange(startNumber, endNumber);
    }

}
