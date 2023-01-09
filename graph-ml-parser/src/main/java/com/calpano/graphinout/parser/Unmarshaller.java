package com.calpano.graphinout.parser;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graph.GioGraphML;
import java.io.File;

/**
 *
 * @author rbaba
 */
@FunctionalInterface
public interface Unmarshaller {

    GioGraphML unmarshall(File sourceFile, String inputSourceStructureID) throws GioException;
}
