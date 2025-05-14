package org.alexbyid.solver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.alexbyid.solver.dto.MatrixDTO;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;

@Getter
public class LatinSquareSolver {
    private final int size;
    private final String[][] matrix;
    private final String[] symbols;
    private final MatrixDTO matrixData;

    private static final ObjectMapper mapper = new ObjectMapper();

    public LatinSquareSolver(MatrixDTO matrixData) {
        if(matrixData.getSize() >= 1)
            size = matrixData.getSize();
        else throw new IllegalArgumentException();
        matrix = new String[size][size];
        symbols = generateSymbols(size);
        this.matrixData = matrixData;

        initializePredefinedCells();
    }

    private String[] generateSymbols(int size){
        String[] symbols = new String[size];
        for(int i = 0; i < size; i++){
            symbols[i] = String.valueOf((char) ('A' + i));
        }
        return symbols;
    }

    private void initializePredefinedCells(){
        Set<MatrixDTO.Cell> cells = matrixData.getPredefined();
        if(cells != null) {
            for (MatrixDTO.Cell cell : cells) {
                if (cell.getRow() != 1 || cell.getCol() != 1) {
                    matrix[cell.getCol() - 1][cell.getCol() - 1] = cell.getValue();
                }
            }
        }
    }

    public ObjectNode solveRange(BigInteger start, BigInteger end){
        ObjectNode result = mapper.createObjectNode();
        long startTime = System.currentTimeMillis();

        BigInteger current = start;
        while (current.compareTo(end) <= 0) {
            if (checkCombination(current, result)) {
                result.remove("matrix");
                result.put("solution", mapper.valueToTree(matrix));
                result.put("solutionNumber", current.toString());
                break;
            }
            current = current.add(BigInteger.ONE);
            resetMatrix(matrix);
            initializePredefinedCells();
        }

        result.put("executionTime", System.currentTimeMillis() - startTime);
        return result;
    }

    private boolean checkCombination(BigInteger number, ObjectNode result){
        BigInteger current = number;
        BigInteger base = BigInteger.valueOf(size);

        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(matrix[i][j] == null){
                    BigInteger[] divRem = current.divideAndRemainder(base);
                    int symbolIndex = divRem[1].intValue();
                    matrix[i][j] = symbols[symbolIndex];
                    current = divRem[0];
                }
            }
        }
        result.put("matrix", mapper.valueToTree(matrix));
        return isValidLatinSquare();
    }

    private void resetMatrix(String[][] matrix){
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                matrix[i][j] = null;
            }
        }
    }

    private boolean isValidLatinSquare() {
        for (String[] row : matrix) {
            if (!isValidSet(row)) return false;
        }

        for (int j = 0; j < size; j++) {
            String[] column = new String[size];
            for (int i = 0; i < size; i++) {
                column[i] = matrix[i][j];  // Формируем столбец
            }
            if (!isValidSet(column)) return false;
        }
        return true;
    }

    private boolean isValidSet(String[] set) {
        boolean[] seen = new boolean[size];
        for (String s : set) {
            int index = Arrays.asList(symbols).indexOf(s);
            if (index == -1 || seen[index]) return false;
            seen[index] = true;
        }
        return true;
    }
}
