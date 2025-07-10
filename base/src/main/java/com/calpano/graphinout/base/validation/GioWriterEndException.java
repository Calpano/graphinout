package com.calpano.graphinout.base.validation;

public class GioWriterEndException extends GioWriterException {


    public GioWriterEndException(ValidatingGioWriter.CurrentElement offendingElement, ValidatingGioWriter.CurrentElement lastStartedElement, String msg) {
        super(offendingElement, lastStartedElement, msg);

    }

}
