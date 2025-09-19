package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjEdge;
import com.calpano.graphinout.base.cj.element.ICjEndpoint;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjPort;
import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEdgeBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CjDocument2Graphml {

    private  final  GraphmlWriter gWriter;

    public CjDocument2Graphml(GraphmlWriter gWriter) {this.gWriter = gWriter;
    }

    public static void writeTo(ICjDocument cjDoc, GraphmlWriter gWriter) throws IOException {
        new CjDocument2Graphml(gWriter).write(cjDoc);
    }

    public void write(ICjDocument cjDoc) throws IOException {
        GraphmlDocumentBuilder gDoc = IGraphmlDocument.builder();
        // map _SOME_ cjData to native graphMl constructs
        cjDoc.onDataValue(json -> {

            json.resolve("description", desc -> gDoc.desc(IGraphmlDescription.builder().value(//
                    desc.asPrimitive().castTo(String.class) //
            ).build()));

            json.resolve("cj:attributes", atts -> {
                atts.onProperties((k, v) -> {
                    gDoc.attribute(k, v.asPrimitive().castTo(String.class));
                });
            });
        });

        // cj:Document.baseUri -> graphml has no baseuri, so: <data>
        // FIXME put baseUri to <data>, prepare <key>
        cjDoc.baseUri();

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

    public void write(ICjGraph cjGraph) throws IOException {
        // TODO nodes, edges, sub-graphs
    }

    public void write(ICjNode cjNode) throws IOException {
        // TODO ...
    }

    // same for cj edge, endpoint, port

    public void write(ICjEdge cjEdge) throws IOException {
        // TODO ...

        GraphmlEdgeBuilder edgeBuilder = IGraphmlEdge.builder();
        cjEdge.endpoints().forEach(cjEp->{

        });

        gWriter.edgeStart(edgeBuilder.build());
        // TODO ???
        gWriter.edgeEnd();
    }

    public void write(ICjEndpoint cjEndpoint) throws IOException {
        // TODO ...

        GraphmlDirection gDir = GraphmlDirection.ofCj(cjEndpoint.direction());

    }

    public void write(ICjPort cjPort) throws IOException {
        // TODO ...
    }

                      /** data@id -> key@for -> <key> */
    private final Map<String, Map<GraphmlKeyForType, IGraphmlKey>> dataId_for_key = new HashMap<>();


}
