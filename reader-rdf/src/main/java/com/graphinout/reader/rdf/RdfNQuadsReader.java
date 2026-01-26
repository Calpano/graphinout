package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class RdfNQuadsReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "rdf.nq";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "N-Quads", ".nq");

    public RdfNQuadsReader() {super(FORMAT, RdfFormats.RdfSyntax.N_QUADS);}

}
