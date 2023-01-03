package com.calpano.graphinout.parser;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graph.GioGraphML;

/**
 *
 * @author rbaba
 */
@FunctionalInterface
public interface Unmarshaller {

    GioGraphML unmarshall(String fileName) throws GioException;
}
