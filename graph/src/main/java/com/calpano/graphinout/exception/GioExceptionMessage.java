/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calpano.graphinout.exception;

import lombok.Getter;

/**
 *
 * @author rbaba
 */
//ُTODO
//This class needs to review the design and optimize the messages
@Getter
public enum GioExceptionMessage implements GioExceptionCode {
    UNMARSHALLING_ERROR("GIO_001", "Unmarshal failed ", ErrorPriority.HIGH, ErrorProvider.JAVA),
    MARSHALLING_ERROR("GIO_003", "Marshal invalid", ErrorPriority.HIGH, ErrorProvider.JAVA),
    XML_FILE_INVALID("GIO_004", "File invalid", ErrorPriority.HIGH, ErrorProvider.VALIDATOR),
    ;

    private final String errorCode;
    private final String errorMessage;
    private final ErrorPriority priority;
    private final ErrorProvider provider;

    private GioExceptionMessage(String errorCode, String errorMessage, ErrorPriority priority, ErrorProvider provider) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.priority = priority;
        this.provider = provider;
    }

}
