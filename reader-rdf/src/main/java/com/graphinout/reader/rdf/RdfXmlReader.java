package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class RdfXmlReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "rdf.xml";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "RDF/XML", ".rdf", ".rdf.xml");

    public RdfXmlReader() {super(FORMAT, RdfFormats.RdfSyntax.RDF_XML);}

}
