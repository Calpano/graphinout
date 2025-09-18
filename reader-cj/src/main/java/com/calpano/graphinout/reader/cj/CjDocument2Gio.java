package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioWriter;

import java.io.IOException;

public class CjDocument2Gio {

    public static void write(ICjDocument cjDoc, GioWriter gio) throws IOException {
        GioDocument.GioDocumentBuilder builder = GioDocument.builder();
        gio.startDocument(builder
                .build());
    }

}
