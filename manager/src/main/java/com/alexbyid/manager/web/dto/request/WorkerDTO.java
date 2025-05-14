package com.alexbyid.manager.web.dto.request;

import com.alexbyid.manager.enums.WorkerStatusEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class WorkerDTO {
    private final String host;
    private final int port;
    @Setter
    private WorkerStatusEnum workerStatus;
    private static final String CHECK_STATE_ENDPOINT = "check-state";

    public WorkerDTO(String host, int port) {
        this.host = host;
        this.port = port;
        workerStatus = WorkerStatusEnum.UNINITIALIZED;
    }

    public String fullAddress() {
        return String.format("http://%s:%d", host, port);
    }

    public String checkStateAddress() {
        return String.format("%s/%s", fullAddress(), CHECK_STATE_ENDPOINT);
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
