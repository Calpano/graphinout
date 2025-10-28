package com.graphinout.base.cj;

import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.reader.Locator;
import com.graphinout.foundation.input.IHandleContentErrors;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BaseCjOutput extends CjFactory implements IHandleContentErrors {

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
