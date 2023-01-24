package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.gio.GioDocument;

import java.io.File;

/**
 * @author rbaba
 */
@FunctionalInterface
public interface Unmarshaller {

    GioDocument unmarshall(File sourceFile, File outputFile, String inputSourceStructureID) throws GioException;
}
