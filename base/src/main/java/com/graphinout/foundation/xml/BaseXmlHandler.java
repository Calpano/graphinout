package com.graphinout.foundation.xml;

import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.reader.Locator;
import com.graphinout.foundation.input.IHandleContentErrors;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BaseXmlHandler implements IHandleContentErrors {

    private Consumer<ContentError> errorHandler;
    private Locator locator;

    @Override
    public Consumer<ContentError> contentErrorHandler() {
        return this.errorHandler;
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
