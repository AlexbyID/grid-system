package com.alexbyid.distributor.system.dto;

import com.alexbyid.distributor.enums.WorkerStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class WorkerDTO {

    private String host;
    private int port;
    @Setter
    private WorkerStatusEnum workerStatus;
    private static final String INIT_ENDPOINT = "init";
    private static final String SOLVE_ENDPOINT = "solve";
    private static final String RESET_ENDPOINT = "reset";

    public WorkerDTO() {}

    public WorkerDTO(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String fullAddress() {
        return String.format("http://%s:%d", host, port);
    }

    public String initAddress() {
        return String.format("%s/%s", fullAddress(), INIT_ENDPOINT);
    }
    public String solveAddress() {
        return String.format("%s/%s", fullAddress(), SOLVE_ENDPOINT);
    }
    public String resetAddress() {
        return String.format("%s/%s", fullAddress(), RESET_ENDPOINT);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorkerDTO workerDTO = (WorkerDTO) o;
        return port == workerDTO.port && Objects.equals(host, workerDTO.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
