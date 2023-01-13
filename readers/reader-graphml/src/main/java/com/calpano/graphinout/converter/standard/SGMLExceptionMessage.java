/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.exception.ErrorPriority;
import com.calpano.graphinout.exception.ErrorProvider;
import com.calpano.graphinout.exception.GioExceptionCode;
import lombok.Getter;

/**
 * @author rbaba
 */
@Getter
public enum SGMLExceptionMessage implements GioExceptionCode {
    SGML_FILE_INCOMPATIBLE("SGML_001", "The file is incompatible with the specified format", ErrorPriority.HIGH, ErrorProvider.VALIDATOR),

    XML_FILE_ERROR("GIO_003", "Marshal invalid", ErrorPriority.HIGH, ErrorProvider.JAVA),
    ;
    private final String errorCode;
    private final String errorMessage;
    private final ErrorPriority priority;
    private final ErrorProvider provider;

    private SGMLExceptionMessage(String errorCode, String errorMessage, ErrorPriority priority, ErrorProvider provider) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.priority = priority;
        this.provider = provider;
    }

}
