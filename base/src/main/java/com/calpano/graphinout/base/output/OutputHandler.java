package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.exception.GioException;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * For writing GraphML structures (nodes, hyperedges, metadata such as key) in a streaming fashion
 * @param <T>
 */
public interface OutputHandler<T> extends GraphMlWriter{

    public void initialize(T t) throws GioException;


    public void outputFinalize() throws GioException;

}
