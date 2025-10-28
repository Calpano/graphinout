package com.graphinout.base.gio;

import com.graphinout.base.cj.BaseCjOutput;
import com.graphinout.base.cj.CjFactory;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.element.ICjData;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEndpoint;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjLabel;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjPort;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.java.JavaJsonObject;
import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.io.IOException;

@Deprecated
public class CjStream2GioWriter extends BaseCjOutput implements ICjStream {

    private final GioWriter gioWriter;

    public CjStream2GioWriter(GioWriter gioWriter) {this.gioWriter = gioWriter;}

    private static GioEndpoint toGioEndpoint(ICjEndpoint ep) {
        GioEndpointDirection dir = switch (ep.direction()) {
            case IN -> GioEndpointDirection.In;
            case OUT -> GioEndpointDirection.Out;
            case UNDIR -> GioEndpointDirection.Undirected;
        };
        return GioEndpoint.builder()
                .id(null)
                .node(ep.node())
                .port(ep.port())
                .type(dir)
                .build();
    }

    @Override
    public void documentEnd() {
        try {
            gioWriter.endDocument();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        try {
            gioWriter.startDocument(new GioDocument());
            // baseUri
            if (document.baseUri() != null) {
                gioWriter.baseUri(document.baseUri());
            }
            // CJ data as one blob
            emitCjDataIfPresent(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void edgeEnd() {
        try {
            gioWriter.endEdge();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        try {
            GioEdge ge = GioEdge.builder()
                    .id(edge.id())
                    .build();
            // endpoints
            edge.endpoints().forEach(ep -> ge.addEndpoint(toGioEndpoint(ep)));
            gioWriter.startEdge(ge);
            // edge type
            @Nullable ICjEdgeType et = edge.edgeType();
            if (et != null) {
                String json = ICjEdgeType.toJsonString(et);
                GioData d = GioData.builder()
                        .key(CjGraphmlMapping.GraphmlDataElement.EdgeType.attrName)
                        .xmlValue(XmlFragmentString.ofPlainText(json))
                        .build();
                gioWriter.data(d);
            }
            // label
            emitLabelIfPresent(edge);
            // data
            emitCjDataIfPresent(edge);
            // Immediately close to let Gio2CjStream buffer it
            gioWriter.endEdge();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void graphEnd() {
        try {
            gioWriter.endGraph(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        try {
            GioGraph gg = GioGraph.builder()
                    .id(graph.id())
                    .edgedefaultDirected(false)
                    .build();
            gioWriter.startGraph(gg);
            emitCjDataIfPresent(graph);
            emitLabelIfPresent(graph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nodeEnd() {
        try {
            gioWriter.endNode(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        try {
            GioNode gn = GioNode.builder()
                    .id(node.id())
                    .build();
            gioWriter.startNode(gn);
            // emit ports recursively
            node.ports().forEach(p -> {
                try {
                    emitPortRecursively(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            emitCjDataIfPresent(node);
            emitLabelIfPresent(node);
            // close immediately to let Gio2CjStream buffer it
            gioWriter.endNode(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void emitCjDataIfPresent(com.graphinout.base.cj.element.ICjHasData cjHasData) throws IOException {
        ICjData data = cjHasData.data();
        if (data == null) return;
        IJsonValue val = data.jsonValue();
        if (val == null) return;
        // Do not include GraphML-specific properties in the blob to avoid conflicts
        if (val.isObject()) {
            JavaJsonObject mutable = JavaJsonObject.copyOf(val.asObject());
            mutable.removePropertyIf(k -> k.startsWith("graphml:"));
            if (!mutable.isEmpty()) {
                GioData d = GioData.builder()
                        .key(CjGraphmlMapping.GraphmlDataElement.CjJsonData.attrName)
                        .xmlValue(XmlFragmentString.ofPlainText(mutable.toJsonString()))
                        .build();
                gioWriter.data(d);
            }
        } else {
            GioData d = GioData.builder()
                    .key(CjGraphmlMapping.GraphmlDataElement.CjJsonData.attrName)
                    .xmlValue(XmlFragmentString.ofPlainText(val.toJsonString()))
                    .build();
            gioWriter.data(d);
        }
    }

    private void emitLabelIfPresent(ICjPort port) throws IOException {
        ICjLabel label = port.label();
        if (label == null) return;
        String value;
        if (label.entries().count() == 1 && label.entries().toList().getFirst().language() == null) {
            value = label.entries().toList().getFirst().value();
        } else {
            value = label.toJsonString();
        }
        GioData d = GioData.builder()
                .key(CjGraphmlMapping.GraphmlDataElement.Label.attrName)
                .xmlValue(XmlFragmentString.ofPlainText(value))
                .build();
        gioWriter.data(d);
    }

    private void emitLabelIfPresent(com.graphinout.base.cj.element.ICjHasLabel cjHasLabel) throws IOException {
        ICjLabel label = cjHasLabel.label();
        if (label == null) return;
        String value;
        if (label.entries().count() == 1 && label.entries().toList().getFirst().language() == null) {
            value = label.entries().toList().getFirst().value();
        } else {
            value = label.toJsonString();
        }
        GioData d = GioData.builder()
                .key(CjGraphmlMapping.GraphmlDataElement.Label.attrName)
                .xmlValue(XmlFragmentString.ofPlainText(value))
                .build();
        gioWriter.data(d);
    }

    private void emitPortRecursively(ICjPort port) throws IOException {
        String name = port.id() != null ? port.id() : "";
        GioPort gp = GioPort.builder().name(name).build();
        gioWriter.startPort(gp);
        // emit label and data attached to this port
        emitLabelIfPresent(port);
        emitCjDataIfPresent(port);
        // recurse into child ports
        port.ports().forEach(p -> {
            try {
                emitPortRecursively(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        gioWriter.endPort();
    }

}
