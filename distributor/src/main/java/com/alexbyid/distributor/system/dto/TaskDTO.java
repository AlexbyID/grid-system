package com.alexbyid.distributor.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
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
    
}
