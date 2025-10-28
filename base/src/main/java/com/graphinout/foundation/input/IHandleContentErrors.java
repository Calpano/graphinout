package com.graphinout.foundation.input;

import com.graphinout.base.reader.Location;
import com.graphinout.base.reader.Locator;
import org.slf4j.Logger;

import javax.annotation.Nullable;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Helper for working with {@link ContentError}
 */
public interface IHandleContentErrors extends ContentErrorAware, LocationAware {

    Logger _log = getLogger(IHandleContentErrors.class);

    /**
     * @param message       additional to baseException. Location info is added automatically.
     * @param baseException cause
     * @return an exception to be thrown at the crime scene where the issues happened
     */
    default ContentErrorException sendContentError_Error(String message, @Nullable Throwable baseException) {
        Location location = Locator.locationOrNotAvailable(locator());
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, "While parsing " + location + "\n" + "Message: " + message, location);
        onContentError(contentError);
        return ContentErrorException.of(contentError,baseException);
    }

    default ContentErrorException sendContentError_Error(String message) {
        return sendContentError_Error(message, null);
    }

    default void sendContentError_Warn(String message, Throwable baseException) {
        Location location = Locator.locationOrNotAvailable(locator());
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Warn, message, location);
        onContentError(contentError);
    }

}
