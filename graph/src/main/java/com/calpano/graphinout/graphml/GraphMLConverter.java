package com.calpano.graphinout.graphml;

import com.calpano.graphinout.exception.GioException;

import java.io.File;

/**
 * @param <T> outputObject
 * @author rbaba
 */
@FunctionalInterface
public interface GraphMLConverter<T> {

    T convert(File inputFile, GraphMLService graphMLService) throws GioException;

}
