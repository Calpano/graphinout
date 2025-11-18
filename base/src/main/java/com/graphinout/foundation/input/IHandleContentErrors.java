package com.graphinout.foundation.input;

import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Helper for working with {@link ContentError}
 */
public interface IHandleContentErrors extends ContentErrorAware, LocationAware {

    Logger _log = getLogger(IHandleContentErrors.class);

    /**
     * One of 'message' or 'baseException' should be given.
     *
     * @param message       optional
     * @param baseException cause
     * @param locator
     * @return an exception to be thrown at the crime scene where the issues happened
     */
    default ContentErrorException sendContentError_Error(@Nullable String message, @Nullable Throwable baseException, @Nullable Locator locator) {
        Location location = Locator.locationOrNotAvailable(locator);
        String exMsg = null;
        if (baseException != null) {
            exMsg = baseException.getMessage();
            if (exMsg == null) {
                exMsg = baseException.getClass().getSimpleName();
            }
        }
        String msg;
        if (message == null) {
            msg = Objects.requireNonNullElse(exMsg, "Unknown error");
        } else {
            msg = message + (exMsg == null ? "" : ". " + exMsg);
        }
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, msg, location);
        onContentError(contentError);
        return ContentErrorException.of(contentError, baseException);
    }

    default ContentErrorException sendContentError_Error(String message) {
        return sendContentError_Error(message, null, null);
    }

    default void sendContentError_Warn(String message, Throwable baseException) {
        Location location = Locator.locationOrNotAvailable(locator());
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Warn, message, location);
        onContentError(contentError);
    }

}
