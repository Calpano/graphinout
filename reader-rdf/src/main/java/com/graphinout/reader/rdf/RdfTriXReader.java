package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class RdfTriXReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "trix";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "TriX", ".trix");

    public RdfTriXReader() {super(FORMAT, RdfFormats.RdfSyntax.TRIX);}

}
