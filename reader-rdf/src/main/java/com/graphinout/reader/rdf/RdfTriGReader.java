package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class RdfTriGReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "rdf.trig";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "TriG", ".trig");

    public RdfTriGReader() {super(FORMAT, RdfFormats.RdfSyntax.TRIG);}

}
