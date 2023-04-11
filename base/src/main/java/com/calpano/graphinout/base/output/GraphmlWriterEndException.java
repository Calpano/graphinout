package com.calpano.graphinout.base.output;

public class GraphmlWriterEndException extends IllegalStateException {

    public final ValidatingGraphMlWriter.CurrentElement offendingElement;
    public final ValidatingGraphMlWriter.CurrentElement lastStartedElement;

    public GraphmlWriterEndException(ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, String msg) {
        super(msg);
        this.offendingElement = offendingElement;
        this.lastStartedElement = lastStartedElement;
    }

}
