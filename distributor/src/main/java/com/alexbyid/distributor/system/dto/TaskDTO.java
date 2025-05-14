package com.alexbyid.distributor.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class TaskDTO {

    private String start;
    private String count;
    private String callbackUrl;
    @JsonIgnore
    private String worker;

    public TaskDTO() {}

    public TaskDTO(String start, String count, String callbackUrl) {
        this.start = start;
        this.count = count;
        this.callbackUrl = callbackUrl;
        this.worker = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TaskDTO taskDTO = (TaskDTO) o;
        return Objects.equals(start, taskDTO.start) && Objects.equals(count, taskDTO.count) && Objects.equals(callbackUrl, taskDTO.callbackUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, count, callbackUrl);
    }
}
