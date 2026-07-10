package com.graphinout.engine;

import com.graphinout.base.gio.GioReader;

import java.io.File;
import java.util.List;

/**
 * Facade for the GraphInOut engine, providing high-level operations for graph processing.
 */
public class GioEngine {

    private static final GioEngineCore core = new GioEngineCore();

    // TODO finish this implementation
    /**
     * Reads an input file and writes it to an output directory in GraphML format.
     *
     * @param inputFile       the input file to read.
     * @param outputDirectory the directory to write the output GraphML file to.
     */
    public static void readFileToGraphMl(File inputFile, File outputDirectory) {
        // determine which Readers can handle the input file
        // list all readers in GioServiceLoader
        List<GioReader> readers = core.readers().stream().filter(gioReader ->
                // can reader handle the input file?
                gioReader.fileFormat().matches(inputFile.getPath())).toList();
        for (GioReader r : readers) {
            System.out.println(r.fileFormat().id());
            // TODO read inputfile into string
        }
    }


}
