package com.calpano.graphinout.base.validation;

public abstract class GioWriterException extends IllegalStateException {

    public final ValidatingGioWriter.CurrentElement offendingElement;
    public final ValidatingGioWriter.CurrentElement lastStartedElement;

    public GioWriterException(ValidatingGioWriter.CurrentElement offendingElement, ValidatingGioWriter.CurrentElement lastStartedElement, String msg) {
        super(msg);
        this.offendingElement = offendingElement;
        this.lastStartedElement = lastStartedElement;
    }

}
