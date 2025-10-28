package com.graphinout.foundation.xml;

import com.graphinout.foundation.input.ContentError;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.function.Consumer;

public record SimpleSaxErrorHandler(Consumer<ContentError> errorConsumer) implements ErrorHandler {

    @Override
    public void error(SAXParseException e) throws SAXException {
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, e.getMessage(), null);
        if (errorConsumer != null) {
            errorConsumer.accept(contentError);
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Error, e.getMessage(), null);
        if (errorConsumer != null) {
            errorConsumer.accept(contentError);
        }
        throw e;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        ContentError contentError = new ContentError(ContentError.ErrorLevel.Warn, e.getMessage(), null);
        if (errorConsumer != null) {
            errorConsumer.accept(contentError);
        }
    }

}
