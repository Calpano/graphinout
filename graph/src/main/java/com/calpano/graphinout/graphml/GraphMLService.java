package com.calpano.graphinout.graphml;

import java.util.List;

/**
 * @param <T>
 * @author rbaba
 */
public interface GraphMLService<T> {

    public String getId();

    public InputSourceStructure getInputSourceStructure();

    public GraphMLValueMapper getXMlValueMapper();

    public GraphMLFileValidator getXMLFileValidator();

    public List<GraphMLValidator> getValueValidators();

    public GraphMLConverter<T> getConverter();





}
