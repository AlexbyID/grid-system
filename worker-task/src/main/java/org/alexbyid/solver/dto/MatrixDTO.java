package org.alexbyid.solver.dto;

import java.util.Set;

public class MatrixDTO {
    private int size;
    private Set<Cell> predefined;

    public int getSize() {
        return size;
    }

    public Set<Cell> getPredefined() {
        return predefined;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPredefined(Set<Cell> predefined) {
        this.predefined = predefined;
    }

    public static class Cell {
        private int row;
        private int col;
        private String value;

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public String getValue() {
            return value;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
