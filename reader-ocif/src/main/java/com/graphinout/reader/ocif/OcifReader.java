package com.graphinout.reader.ocif;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.java.JavaJsonValues;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.graphinout.foundation.json.path.IJsonContainerNavigationStep.of;
import static com.graphinout.foundation.json.path.IJsonContainerNavigationStep.pathOf;
import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.graphinout.foundation.util.Nullables.nonNullOrDefault;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class OcifReader implements GioReader {

    public static final String FORMAT_ID = "ocif";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "OCIF Open Canvas Interchange Format (OCIF v0.6)", ".ocif.json", ".ocif");
    private static final Logger log = LoggerFactory.getLogger(OcifReader.class);

    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        if (!(inputSource instanceof SingleInputSource)) {
            throw new IllegalArgumentException("Expected SingleInputSource");
        }
        SingleInputSource sis = (SingleInputSource) inputSource;

        String json;
        try (sis) {
            json = IOUtils.toString(sis.inputStream(), UTF_8);
        }

        // Parse OCIF JSON and emit CJ stream events
        IJsonValue root = JavaJsonValues.ofJsonString(json);
        IJsonObject o = root == null ? null : root.asObject();
        if (o == null) {
            if (errorHandler != null) {
                errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "Invalid OCIF: root must be a JSON object", null));
            }
            throw new IOException("Invalid OCIF: Root element must be a JSON object");
        }

        ICjDocumentChunkMutable doc = cjStream.createDocumentChunk();
        // Attach OCIF document-level data under data.ocif.* to keep roundtrip lossless (e.g., resources, schemas, ocif uri)
        doc.dataMutable(dm -> {
            // keep OCIF schema uri if present
            ifPresentAccept(o.get("ocif"), v -> dm.add(pathOf("ocif", "schemaUri"), v));
            ifPresentAccept(o.get("resources"), v -> dm.add(pathOf("ocif", "resources"), v));
            ifPresentAccept(o.get("schemas"), v -> dm.add(pathOf("ocif", "schemas"), v));
            // Record if input explicitly had a relations property (even if empty)
            if (o.hasProperty("relations")) {
                dm.add(pathOf("ocif", "flags", "hasRelationsProperty"), dm.factory().createBoolean(true));
            }
            // any other root-level extras not mapped go to ocif.extra
            IJsonObjectMutable extra = dm.factory().createObjectMutable();
            Set<String> knownRootKeys = new HashSet<>(Arrays.asList("nodes", "relations", "resources", "schemas", "ocif"));
            for (String key : o.keys()) {
                if (!knownRootKeys.contains(key)) {
                    extra.setProperty(key, o.get(key));
                }
            }
            if (!extra.isEmpty()) {
                dm.add(pathOf("ocif", "extra"), extra);
            }
        });

        cjStream.documentStart(doc);

        // Create single graph to hold nodes/relations
        com.graphinout.base.cj.document.ICjGraphChunkMutable graph = cjStream.createGraphChunk();
        cjStream.graphStart(graph);

        // Nodes
        IJsonValue nodesVal = o.get("nodes");
        if (nodesVal != null && nodesVal.isArray()) {
            IJsonArray nodes = nodesVal.asArray();
            for (int i = 0; i < nodes.size(); i++) {
                IJsonObject nodeObj = nodes.get_(i).asObject();
                ICjNodeChunkMutable node = cjStream.createNodeChunk();
                ifPresentAccept(nodeObj.get("id"), IJsonValue::asString, node::id);

                // map known OCIF node fields under data.ocif.node.* to keep structure
                node.dataMutable(dm -> {
                    ifPresentAccept(nodeObj.get("position"), v -> dm.add(pathOf("ocif", "node", "position"), v));
                    ifPresentAccept(nodeObj.get("size"), v -> dm.add(pathOf("ocif", "node", "size"), v));
                    ifPresentAccept(nodeObj.get("resource"), v -> dm.add(pathOf("ocif", "node", "resource"), v));
                    ifPresentAccept(nodeObj.get("type"), v -> dm.add(pathOf("ocif", "node", "type"), v));
                    ifPresentAccept(nodeObj.get("data"), v -> dm.add(pathOf("ocif", "node", "data"), v));
                    // preserve any unknown fields
                    IJsonObjectMutable extras = dm.factory().createObjectMutable();
                    Set<String> knownNodeKeys = new HashSet<>(Arrays.asList("id", "position", "size", "resource", "type", "data"));
                    for (String nk : nodeObj.keys()) {
                        if (!knownNodeKeys.contains(nk)) {
                            extras.setProperty(nk, nodeObj.get(nk));
                        }
                    }
                    if (!extras.isEmpty()) {
                        dm.add(pathOf("ocif", "node", "extra"), extras);
                    }
                });
                cjStream.nodeStart(node);
                cjStream.nodeEnd();
            }
        }

        // Relations -> edges
        IJsonValue relsVal = o.get("relations");
        if (relsVal != null && relsVal.isArray()) {
            IJsonArray rels = relsVal.asArray();
            for (int i = 0; i < rels.size(); i++) {
                IJsonObject rel = rels.get_(i).asObject();
                com.graphinout.base.cj.document.ICjEdgeChunkMutable edge = cjStream.createEdgeChunk();
                // endpoints depend on relation type

                String type = nonNullOrDefault(rel.get("type"), IJsonValue::asString, "@ocif/rel/edge");
                switch (type) {
                    case "@ocif/rel/parent-child" -> {
                        String parent = requireNonNull(rel.get("parent")).asString();
                        String child = requireNonNull(rel.get("child")).asString();
                        edge.addEndpoint(ep -> ep.node(parent));
                        edge.addEndpoint(ep -> ep.node(child));
                    }
                    case "@ocif/rel/edge" -> {
                        // endpoints array with from/to
                        if (rel.hasProperty("from") && rel.hasProperty("to")) {
                            edge.addEndpoint(ep -> ep.node(rel.get_("from").asString()));
                            edge.addEndpoint(ep -> ep.node(rel.get_("to").asString()));
                        }
                    }
                    case "@ocif/rel/hyperedge" -> {
                        IJsonValue epsVal = rel.get("endpoints");
                        if (epsVal != null && epsVal.isArray()) {
                            IJsonArray eps = epsVal.asArray();
                            for (int j = 0; j < eps.size(); j++) {
                                IJsonObject eo = eps.get_(j).asObject();
                                edge.addEndpoint(ep -> {
                                    ifPresentAccept(eo.get("node"), IJsonValue::asString, ep::node);
                                    ifPresentAccept(eo.get("port"), IJsonValue::asString, ep::port);
                                });
                            }
                        }
                    }
                    default -> log.warn("Unknown relation type '{}'", type);
                }
                // carry relation fields into edge.data.ocif.relation
                edge.dataMutable(dm -> {
                    IJsonContainerNavigationStep ocif = of("ocif");
                    IJsonContainerNavigationStep relStep = of("relation");
                    for (String rk : rel.keys()) {
                        dm.add(List.of(ocif, relStep, of(rk)), rel.get(rk));
                    }
                });
                cjStream.edgeStart(edge);
                cjStream.edgeEnd();
            }
        }

        cjStream.graphEnd();
        cjStream.documentEnd();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
