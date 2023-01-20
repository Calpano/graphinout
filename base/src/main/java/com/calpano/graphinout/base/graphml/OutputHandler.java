package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.exception.GioException;


import java.util.Map;

/**
 * For writing GraphML structures (nodes, hyperedges, metadata such as key) in a streaming fashion
 * @param <T>
 */
public interface OutputHandler<T> {

    public void initialize(T t) throws GioException;
    public void startElement(String name, Map<String,String> attributes) throws GioException;
    public void startElement(String name) throws GioException;

    public void addValue(String...values) throws GioException;
    public void endElement(String name) throws GioException;

    public void outputFinalize() throws GioException;

}
