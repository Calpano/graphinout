package com.calpano.graphinout.graphml;

import java.util.List;

/**
 *
 * @author rbaba
 * @param <T>
 */
public interface GraphMLService<T> {

    public String getId();

    public InputSourceStructure getInputSourceStructure();

    public GraphMLValueMapper getXMlValueMapper();

    public GraphMLFileValidator getXMLFileValidator();

    public List<GraphMLValidator> getValueValidators();

    public GraphMLConverter<T> getConverter();

}
