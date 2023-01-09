package com.calpano.graphinout.parser;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graph.GioGraphML;

/**
 *
 * @author rbaba
 */
@FunctionalInterface
public interface Marshaller {

    void marshall(String fileName, GioGraphML graphML) throws GioException;
}
