package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class RdfNTriplesReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "rdf.nt";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "N-Triples", ".nt");

    public RdfNTriplesReader() {super(FORMAT, RdfFormats.RdfSyntax.N_TRIPLES);}

}
