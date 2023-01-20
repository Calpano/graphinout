package com.calpano.graphinout.base.graphml;

import java.util.List;

/**
 * @param <T>
 * @author rbaba
 */
public interface GraphMLService<T> {

    public String getId();

    public InputSourceStructure getInputSourceStructure();

    // TODO move this into converter only
    public GraphMLValueMapper getXMlValueMapper();

    // TODO move partially into the reader code, partially as a separate step after reading to GraphML
    public GraphMLFileValidator getXMLFileValidator();

    // TODO move partially into the reader code, partially as a separate step after reading to GraphML
    public List<GraphMLValidator> getValueValidators();

    public GraphMLConverter<T> getConverter();





}
