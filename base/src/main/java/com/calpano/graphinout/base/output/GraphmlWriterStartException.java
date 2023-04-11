package com.calpano.graphinout.base.output;

public class GraphmlWriterStartException extends IllegalStateException {
    public final ValidatingGraphMlWriter.CurrentElement currentElement;
    public final ValidatingGraphMlWriter.CurrentElement childElement;

    public GraphmlWriterStartException(ValidatingGraphMlWriter.CurrentElement currentElement, ValidatingGraphMlWriter.CurrentElement childElement, String msg) {
        super(msg);
        this.currentElement = currentElement;
        this.childElement = childElement;
    }
}
