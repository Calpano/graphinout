package com.calpano.graphinout.base.validation;

public class GioWriterStartException extends GioWriterException {

    public GioWriterStartException(ValidatingGioWriter.CurrentElement offendingElement, ValidatingGioWriter.CurrentElement lastStartedElement, String msg) {
        super(offendingElement, lastStartedElement, msg);
    }

}
