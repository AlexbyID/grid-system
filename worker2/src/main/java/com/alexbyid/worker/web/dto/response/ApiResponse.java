package com.alexbyid.worker.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse {

    private final String message;
    private final int statusCode;
    private final Object data;

}
