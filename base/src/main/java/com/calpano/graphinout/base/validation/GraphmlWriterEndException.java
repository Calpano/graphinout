package com.calpano.graphinout.base.validation;

public class GraphmlWriterEndException extends GraphmlWriterException {


    public GraphmlWriterEndException(ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, String msg) {
        super(offendingElement, lastStartedElement, msg);

    }

}
