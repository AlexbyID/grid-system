package com.alexbyid.worker.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManifestDTO {

    private String className;
    private String annotationName;

    public ManifestDTO() {}

}
