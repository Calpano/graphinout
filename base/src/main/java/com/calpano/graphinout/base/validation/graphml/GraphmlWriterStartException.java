package com.calpano.graphinout.base.validation.graphml;

public class GraphmlWriterStartException extends GraphmlWriterException {


    public GraphmlWriterStartException(ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, String msg) {
        super(offendingElement, lastStartedElement, msg);
    }

}
