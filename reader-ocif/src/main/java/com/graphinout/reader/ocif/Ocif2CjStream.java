package com.graphinout.reader.ocif;

import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.util.Nullables;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class Ocif2CjStream implements ICjStream {

    private final Json2StringWriter json2StringWriter = new Json2StringWriter();
    private final @Nullable Consumer<String> onDone;
    private @Nullable String rawOcifJson;

    // Building state for CJ→OCIF
    private com.graphinout.foundation.json.value.IJsonFactory factory;
    private com.graphinout.foundation.json.value.IJsonObjectMutable root;
    private com.graphinout.foundation.json.value.IJsonArrayMutable nodesArr;
    private com.graphinout.foundation.json.value.IJsonArrayMutable relationsArr;
    private boolean hasRelationsProperty;

    public Ocif2CjStream() {
        this(null);
    }

    public Ocif2CjStream(@Nullable Consumer<String> onDone) {
        this.onDone = onDone;
    }

    /**
     * Minimal pass-through support: when set, {@link #resultOcifJsonString()} will return this value unchanged.
     */
    public void setRawOcifJson(String rawOcifJson) {
        this.rawOcifJson = rawOcifJson;
    }

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        // Use concrete CJ elements so OcifReader can fill them
        return new com.graphinout.base.cj.element.impl.CjDocumentElement();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return new com.graphinout.base.cj.element.impl.CjEdgeElement();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return new com.graphinout.base.cj.element.impl.CjGraphElement();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return new com.graphinout.base.cj.element.impl.CjNodeElement();
    }

    @Override
    public void documentEnd() {
        // If raw passthrough set, prefer it
        if (rawOcifJson == null) {
            if (root != null) {
                // Ensure empty relations array is present if it existed in input
                if (this.hasRelationsProperty && (this.relationsArr == null) && !root.hasProperty("relations")) {
                    root.setProperty("relations", factory.createArrayMutable());
                }
                root.fire(json2StringWriter);
            }
        }
        Nullables.ifConsumerPresentAccept(onDone, resultOcifJsonString());
    }

    public String resultOcifJsonString() {
        // Prefer the raw OCIF JSON if provided (round-trip preservation).
        if (rawOcifJson != null) return rawOcifJson;
        return json2StringWriter.jsonString();
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // Initialize factory from document data factory to stay consistent
        this.factory = document.data().factory();
        this.root = factory.createObjectMutable();
        // Restore document-level OCIF info from data.ocif.* if present
        com.graphinout.foundation.json.value.IJsonValue docData = document.data().jsonValue();
        if (docData != null && docData.isObject()) {
            com.graphinout.foundation.json.value.IJsonObject od = docData.asObject();
            if (od.hasProperty("ocif")) {
                com.graphinout.foundation.json.value.IJsonObject oco = od.get("ocif").asObject();
                if (oco.hasProperty("schemaUri")) {
                    root.setProperty("ocif", oco.get("schemaUri"));
                }
                if (oco.hasProperty("resources")) {
                    com.graphinout.foundation.json.value.IJsonValue resVal = oco.get("resources");
                    root.setProperty("resources", deepCloneRecreateStrings(resVal));
                }
                if (oco.hasProperty("schemas")) {
                    root.setProperty("schemas", oco.get("schemas"));
                }
                if (oco.hasProperty("extra")) {
                    com.graphinout.foundation.json.value.IJsonObject extras = oco.get("extra").asObject();
                    extras.keys().forEach(k -> root.setProperty(k, extras.get(k)));
                }
                // read flags
                if (oco.hasProperty("flags")) {
                    com.graphinout.foundation.json.value.IJsonObject flags = oco.get("flags").asObject();
                    if (flags.hasProperty("hasRelationsProperty") && flags.get("hasRelationsProperty").asBoolean()) {
                        this.hasRelationsProperty = true;
                    }
                }
            }
        }
    }

    @Override
    public void edgeEnd() {
        // no-op
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        ensureRelations();
        // Prefer full OCIF relation stored under edge.data.ocif.relation
        com.graphinout.foundation.json.value.IJsonValue v = edge.data() == null ? null : edge.data().jsonValue();
        if (v != null && v.isObject()) {
            com.graphinout.foundation.json.value.IJsonObject eo = v.asObject();
            if (eo.hasProperty("ocif")) {
                com.graphinout.foundation.json.value.IJsonObject ocif = eo.get("ocif").asObject();
                if (ocif.hasProperty("relation")) {
                    relationsArr.add(ocif.get("relation"));
                    return;
                }
            }
        }
        // Fallback: create a minimal relation from endpoints if needed (skipped for now)
    }

    @Override
    public void graphEnd() {
        // no-op
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        // Prepare nodes array on root on demand
        this.nodesArr = factory.createArrayMutable();
        root.setProperty("nodes", nodesArr);
        // Do NOT create relations here; only when first relation arrives to preserve omission of empty arrays
        this.relationsArr = null;
    }

    @Override
    public void nodeEnd() {
        // no-op
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        ensureNodes();
        com.graphinout.foundation.json.value.IJsonObjectMutable no = factory.createObjectMutable();
        if (node.id() != null) {
            no.setProperty("id", factory.createString(node.id()));
        }
        // Restore OCIF node fields from data.ocif.node.*
        com.graphinout.foundation.json.value.IJsonValue dv = node.data() == null ? null : node.data().jsonValue();
        if (dv != null && dv.isObject()) {
            com.graphinout.foundation.json.value.IJsonObject od = dv.asObject();
            if (od.hasProperty("ocif")) {
                com.graphinout.foundation.json.value.IJsonObject ocif = od.get("ocif").asObject();
                if (ocif.hasProperty("node")) {
                    com.graphinout.foundation.json.value.IJsonObject n = ocif.get("node").asObject();
                    if (n.hasProperty("position")) no.setProperty("position", n.get("position"));
                    if (n.hasProperty("size")) no.setProperty("size", n.get("size"));
                    if (n.hasProperty("resource")) no.setProperty("resource", n.get("resource"));
                    if (n.hasProperty("type")) no.setProperty("type", n.get("type"));
                    if (n.hasProperty("data")) no.setProperty("data", n.get("data"));
                    if (n.hasProperty("extra")) {
                        com.graphinout.foundation.json.value.IJsonObject extras = n.get("extra").asObject();
                        extras.keys().forEach(k -> no.setProperty(k, extras.get(k)));
                    }
                }
            }
        }
        nodesArr.add(no);
    }

    private void ensureNodes() {
        if (nodesArr == null) {
            nodesArr = factory.createArrayMutable();
            root.setProperty("nodes", nodesArr);
        }
    }

    private void ensureRelations() {
        if (relationsArr == null) {
            relationsArr = factory.createArrayMutable();
            root.setProperty("relations", relationsArr);
        }
    }

    // Deep-clone a JSON value while recreating all string primitives to guarantee proper escaping on serialization
    private com.graphinout.foundation.json.value.IJsonValue deepCloneRecreateStrings(com.graphinout.foundation.json.value.IJsonValue v) {
        if (v == null) return factory.createNull();
        if (v.isPrimitive()) {
            com.graphinout.foundation.json.value.IJsonPrimitive p = v.asPrimitive();
            Object base = p.base();
            if (base instanceof String s) {
                return factory.createString(s);
            } else if (base instanceof Number n) {
                return factory.createNumber(n);
            } else if (base instanceof Boolean b) {
                return factory.createBoolean(b);
            } else {
                // fallback to toString
                return factory.createString(String.valueOf(base));
            }
        } else if (v.isArray()) {
            com.graphinout.foundation.json.value.IJsonArray a = v.asArray();
            com.graphinout.foundation.json.value.IJsonArrayMutable out = factory.createArrayMutable();
            for (int i = 0; i < a.size(); i++) {
                out.add(deepCloneRecreateStrings(a.get_(i)));
            }
            return out;
        } else { // object
            com.graphinout.foundation.json.value.IJsonObject o = v.asObject();
            com.graphinout.foundation.json.value.IJsonObjectMutable out = factory.createObjectMutable();
            for (String k : o.keys()) {
                out.setProperty(k, deepCloneRecreateStrings(o.get_(k)));
            }
            return out;
        }
    }
}
