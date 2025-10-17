package com.graphinout.base.validation.graphml;

public abstract class GraphmlWriterException extends IllegalStateException {

    public final ValidatingGraphMlWriter.CurrentElement offendingElement;
    public final ValidatingGraphMlWriter.CurrentElement lastStartedElement;

    public GraphmlWriterException(ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, String msg) {
        super(msg);
        this.offendingElement = offendingElement;
        this.lastStartedElement = lastStartedElement;
    }

}
