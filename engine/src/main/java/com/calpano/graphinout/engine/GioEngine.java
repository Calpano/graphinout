package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.reader.GioReader;

import java.io.File;
import java.util.List;

/**
 * Facade
 */
public class GioEngine {

    private static final GioEngineCore core = new GioEngineCore();

    // TODO finish this implementation
    public static void readFileToGraphMl(File inputFile, File outputDirectory) {
        // determine which Readers can handle the input file
        // list all readers in GioServiceLoader
        List<GioReader> readers = core.readers().stream().filter(gioReader ->
                // can reader handle the input file?
                gioReader.fileFormat().matches(inputFile.getPath())).toList();
        for (GioReader r : readers) {
            System.out.println(r.fileFormat().id());
            // read inputfile into string
        }
    }


}
