package com.alexbyid.worker.system.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class WorkerDTO {

    private String host;
    private int port;

    public String fullAddress() {
        return String.format("http://%s:%d", host, port);
    }

}
