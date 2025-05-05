package com.alexbyid.manager.web.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ApiResponse {
    private final String message;
    private final int statusCode;
    private final Object data;
}
