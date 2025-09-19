package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;

import java.io.IOException;

public class CjDocument2Graphml {

    public static void write(ICjDocument cjDoc, GraphmlWriter gWriter) throws IOException {
        GraphmlDocumentBuilder gDoc = IGraphmlDocument.builder();
        // map _SOME_ cjData to native graphMl constructs
        cjDoc.onDataValue(json -> {

            json.resolve("description", desc -> gDoc.desc(desc.asPrimitive().baseT()));

            json.resolve("cj:attributes", atts->{
                atts.onProperties( (k,v)->{
                    gDoc.attribute(k, v.asPrimitive().baseT());
                });
            });
        });

        // <!ELEMENT graphml  (desc?,key*,(data|graph)*)>
        gWriter.documentStart(gDoc.build());
        // TODO keys
//            gio.key(GioKey.builder().forType(GioKeyForType.All) //
//                    .attributeName("json")//
//                    .attributeType(GioDataType.typeString)//
//                    .description("Connected JSON allows custom JSON properties in all objects")//
//                    .build());

        // TODO data
        // emit other cjData as graphMl data


        // TODO graph

        gWriter.documentEnd();
    }

    public static void write(ICjGraph cjGraph, GraphmlWriter gWriter) throws IOException {
        // TODO nodes, edges, sub-graphs

    }

}
