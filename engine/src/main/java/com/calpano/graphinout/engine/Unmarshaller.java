package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.GioGraphML;

import java.io.File;

/**
 * @author rbaba
 */
@FunctionalInterface
public interface Unmarshaller {

    GioGraphML unmarshall(File sourceFile,File outputFile, String inputSourceStructureID) throws GioException;
}
