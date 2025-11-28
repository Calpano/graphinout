package com.graphinout.reader.graphml.validation;

public class GraphmlWriterEndException extends GraphmlWriterException {


    public GraphmlWriterEndException(ValidatingGraphMlWriter.CurrentElement offendingElement, ValidatingGraphMlWriter.CurrentElement lastStartedElement, String msg) {
        super(offendingElement, lastStartedElement, msg);

    }

}
