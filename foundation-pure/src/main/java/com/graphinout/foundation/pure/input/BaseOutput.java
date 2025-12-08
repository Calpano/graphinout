package com.graphinout.foundation.pure.input;


import org.jspecify.annotations.Nullable;
import java.util.function.Consumer;

/**
 * Base class for implementing {@link IHandleContentErrors}.
 */
public class BaseOutput implements IHandleContentErrors {

    private Locator locator;
    private Consumer<ContentError> errorHandler;

    @Nullable
    @Override
    public Consumer<ContentError> contentErrorHandler() {
        return errorHandler;
    }

    @Nullable
    @Override
    public Locator locator() {
        return locator;
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void setLocator(Locator locator) {
        this.locator = locator;
    }

}
