package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class RdfTurtleReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "turtle";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "RDF Turtle", ".ttl", ".n3");

    public RdfTurtleReader() {super(FORMAT, RdfFormats.RdfSyntax.TURTLE);}

}
