package com.graphinout.foundation.xml;

import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Sax2Log implements ErrorHandler {

    private final Logger log;
    int errors = 0;
    int fatals = 0;
    int warnings = 0;

    public Sax2Log(Logger log) {
        this.log = log;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        log.error("SAX error", exception);
        errors++;
    }

    public int errors() {
        return errors;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        log.error("SAX fatal error", exception);
        fatals++;
    }

    public int fatals() {
        return fatals;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        log.warn("SAX warning", exception);
        warnings++;
    }

    public int warnings() {
        return warnings;
    }

}
