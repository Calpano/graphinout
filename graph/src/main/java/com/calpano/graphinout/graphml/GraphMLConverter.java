package com.calpano.graphinout.graphml;

import com.calpano.graphinout.exception.GioException;
import java.io.File;

/**
 *
 * @author rbaba
 * @param <T> outputObject
 */
@FunctionalInterface
public interface GraphMLConverter<T> {

    T convert(File inputFile, GraphMLService graphMLService) throws GioException;

}
