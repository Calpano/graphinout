package com.calpano.graphinout.graphml;

import com.calpano.graphinout.exception.GioException;

import java.util.Map;

public interface OutputHandler<T> {


    public void initialize(T t) throws GioException;
    public void startElement(String name, Map<String,String> attributes) throws GioException;
    public void startElement(String name) throws GioException;
    public void addValue(String...values) throws GioException;
    public void endElement(String name) throws GioException;

    public void outputFinalize() throws GioException;

}
