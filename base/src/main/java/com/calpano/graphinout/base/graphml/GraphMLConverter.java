package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.exception.GraphmlException;

import java.io.File;

/**
 * @param <T> outputObject
 * @author rbaba
 */
@FunctionalInterface
public interface GraphMLConverter<T> {

    T convert(File inputFile, File outputFile,GraphMLService graphMLService)  throws GraphmlException;

}
