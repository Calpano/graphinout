package com.graphinout.foundation.pure.input;

import com.graphinout.foundation.pure.bridge.Java9;
import org.jspecify.annotations.Nullable;


/**
 * Helper for working with {@link ContentError}
 */
public interface IHandleContentErrors extends ContentErrorAware, LocationAware {


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
            msg = Java9.Objects.requireNonNullElse(exMsg, "Unknown error");
        } else {
            msg = message + (exMsg == null ? "" : ". " + exMsg);
        }
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, msg, location);
        onContentError(contentError);
        return ContentErrorException.of(contentError, baseException);
    }

    default ContentErrorException sendContentError_Error(String message, Locator locator) {
        return sendContentError_Error(message, null, locator);
    }

    default void sendContentError_Warn(String message, Throwable baseException) {
        Location location = Locator.locationOrNotAvailable(locator());
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Warn, message, location);
        onContentError(contentError);
    }

}
