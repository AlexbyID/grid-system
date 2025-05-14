package com.alexbyid.worker.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskRequest {

    private final String start;
    private final String count;
    private final String callbackUrl;

}
