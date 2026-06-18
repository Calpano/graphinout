package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

public class JsonLdReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "json-ld";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "JSON-LD", ".jsonld", ".jsonld.json",".jsonld11");

    public JsonLdReader() {super(FORMAT, RdfFormats.RdfSyntax.JSON_LD);}

}
