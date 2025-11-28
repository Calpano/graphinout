package com.graphinout.reader.graphml.validation;

public class GraphmlWriterStartException extends GraphmlWriterException {


    public GraphmlWriterStartException(ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, String msg) {
        super(offendingElement, lastStartedElement, msg);
    }

}
