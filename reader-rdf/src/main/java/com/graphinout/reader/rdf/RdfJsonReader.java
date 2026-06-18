package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class RdfJsonReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "rdf-json";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "RJ (deprecated)", ".rj");

    public RdfJsonReader() {super(FORMAT, RdfFormats.RdfSyntax.RDF_JSON);}

}
