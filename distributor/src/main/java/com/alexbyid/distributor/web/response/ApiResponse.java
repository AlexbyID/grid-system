package com.alexbyid.distributor.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse {

    private String message;
    private int statusCode;
    private Object data;

}
