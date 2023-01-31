
package com.calpano.graphinout.base.exception;

/**
 * @author rbaba
 */
public class GioException extends Exception {

    private final GioExceptionCode exceptionCode;

    public GioException(String errorCode, String errorMessage, ErrorPriority priority, ErrorProvider provider, Throwable cause) {
        super(cause.getMessage(), cause);
        exceptionCode = new GioExceptionCode() {
            @Override
            public String getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorMessage() {
                return errorMessage;
            }

            @Override
            public ErrorPriority getPriority() {
                return priority;
            }

            @Override
            public ErrorProvider getProvider() {
                return provider;
            }
        };
    }

    public GioException(GioExceptionCode exceptionCode, Throwable cause) {
        super(cause.getMessage(), cause);
        this.exceptionCode = exceptionCode;
    }

    public GioException(GioExceptionCode exceptionCode) {
        super(exceptionCode.getErrorMessage());
        this.exceptionCode = exceptionCode;
    }

    public GioExceptionCode getGioExceptionCode() {
        return exceptionCode;
    }

}
