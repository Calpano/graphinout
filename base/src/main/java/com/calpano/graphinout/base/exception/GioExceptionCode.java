
package com.calpano.graphinout.base.exception;

/**
 * @author rbaba
 */
public interface GioExceptionCode {
    public String getErrorCode();

    public String getErrorMessage();

    public ErrorPriority getPriority();

    public ErrorProvider getProvider();
}