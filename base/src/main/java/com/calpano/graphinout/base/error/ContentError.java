package com.calpano.graphinout.base.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContentError {
    private LogLevel logLevel;
    private String message;
    private Location location;
    private ErrorLevel errorLevel;
}
