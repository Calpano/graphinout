package com.graphinout.reader.ocif;

import com.graphinout.base.cj.BaseCjOutput;
import com.graphinout.base.cj.element.ICjData;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonArrayMutable;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonPrimitive;
import com.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifConsumerPresentAccept;
import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

public class Ocif2CjStream extends BaseCjOutput implements ICjStream {

    public static final String OCIF = "ocif";
    public static final String NODE = "node";
    public static final String NODES = "nodes";
    public static final String RELATIONS = "relations";
    public static final String RESOURCES = "resources";
    public static final String SCHEMAS = "schemas";
    public static final String RELATION = "relation";
    private final @Nullable Consumer<String> onDone;

    // Building state for CJ→OCIF
    private IJsonObjectMutable root;
    private IJsonArrayMutable nodesArr;
    private IJsonArrayMutable relationsArr;
    private boolean hasRelationsProperty;

    public Ocif2CjStream() {
        this(null);
    }


    public Ocif2CjStream(@Nullable Consumer<String> onDone) {
        this.onDone = onDone;
    }

    @Override
    public void documentEnd() {
        // If input explicitly had a relations property but no edges were emitted, preserve empty array
        if (hasRelationsProperty && relationsArr == null && root != null) {
            root.setProperty(RELATIONS, jsonFactory().createArrayMutable());
        }
        ifConsumerPresentAccept(onDone, resultOcifJsonString());
    }

    @Override
    public void documentStart(ICjDocumentChunk cjDoc) {
        this.root = jsonFactory().createObjectMutable();
        // Restore document-level OCIF info from data.ocif.* if present
        cjDoc.data(data -> ifPresentAccept(data.jsonValue(), IJsonValue::asObjectOrNull, o -> {
            o.getMaybeAs(OCIF, IJsonValue::asObjectOrNull, ocifObject -> {
                ifPresentAccept(ocifObject.get("schemaUri"), v -> root.setProperty(OCIF, v));
                ifPresentAccept(ocifObject.get(RESOURCES), v -> root.setProperty(RESOURCES, deepCloneRecreateStrings(v)));
                ifPresentAccept(ocifObject.get(SCHEMAS), v -> root.setProperty(SCHEMAS, v));
                ifPresentAccept(ocifObject.get("extra"), IJsonValue::asObject, extras -> extras.keys().forEach(k -> root.setProperty(k, extras.get(k))));
                // read flags
                if (ocifObject.hasProperty("flags")) {
                    IJsonObject flags = ocifObject.get_("flags").asObject();
                    if (flags.hasProperty("hasRelationsProperty") && flags.get_("hasRelationsProperty").asBoolean()) {
                        this.hasRelationsProperty = true;
                    }
                }
            });
        }));
    }

    @Override
    public void edgeEnd() {
        // no-op
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        ensureRelations();
        // Prefer full OCIF relation stored under edge.data.ocif.relation
        edge.data(data -> ifPresentAccept(data.jsonValue(), IJsonValue::asObjectOrNull, eo -> //
                eo.getMaybeAs(OCIF, IJsonValue::asObjectOrNull, ocif -> //
                        ocif.getMaybe(RELATION, rel -> relationsArr.add(rel)))));
        // Fallback: create a minimal relation from endpoints if needed (skipped for now)
    }

    @Override
    public void graphEnd() {
        // no-op
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        // Prepare nodes array on root on demand
        this.nodesArr = jsonFactory().createArrayMutable();
        root.setProperty(NODES, nodesArr);
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
        IJsonObjectMutable data = jsonFactory().createObjectMutable();
        String nodeId = node.id();
        if (nodeId != null) {
            data.setProperty("id", jsonFactory().createString(nodeId));
        }
        // Restore OCIF node fields from data.ocif.node.*
        ICjData nodeDataHolder = node.data();
        IJsonValue dv = nodeDataHolder == null ? null : nodeDataHolder.jsonValue();
        if (dv != null && dv.isObject()) {
            IJsonObject od = dv.asObject();
            if (od.hasProperty(OCIF)) {
                IJsonValue ocifVal = od.get(OCIF);
                if (ocifVal != null && ocifVal.isObject()) {
                    IJsonObject ocif = ocifVal.asObject();
                    if (ocif.hasProperty(NODE)) {
                        IJsonValue nodeVal = ocif.get(NODE);
                        if (nodeVal != null && nodeVal.isObject()) {
                            IJsonObject ocif_node = nodeVal.asObject();
                            ifPresentAccept(ocif_node.get("position"), v -> data.setProperty("position", v));
                            ifPresentAccept(ocif_node.get("size"), v -> data.setProperty("size", v));
                            ifPresentAccept(ocif_node.get("resource"), v -> data.setProperty("resource", v));
                            ifPresentAccept(ocif_node.get("type"), v -> data.setProperty("type", v));
                            ifPresentAccept(ocif_node.get("data"), v -> data.setProperty("data", v));
                            ifPresentAccept(ocif_node.get("extra"), v -> {
                                if (v.isObject()) {
                                    IJsonObject extras = v.asObject();
                                    extras.keys().forEach(k -> data.setProperty(k, extras.get(k)));
                                }
                            });
                        }
                    }
                }
            }
        }
        nodesArr.add(data);
    }

    public String resultOcifJsonString() {
        // serialize current root into a fresh writer to avoid accumulation across calls
        Json2StringWriter w = new Json2StringWriter();
        if (root != null) {
            root.fire(w);
        }
        return w.jsonString();
    }

    // Deep-clone a JSON value while recreating all string primitives to guarantee proper escaping on serialization
    private IJsonValue deepCloneRecreateStrings(IJsonValue v) {
        if (v == null) return jsonFactory().createNull();
        if (v.isPrimitive()) {
            IJsonPrimitive p = v.asPrimitive();
            Object base = p.base();
            return switch (base) {
                case String s -> jsonFactory().createString(s);
                case Number n -> jsonFactory().createNumber(n);
                case Boolean b -> jsonFactory().createBoolean(b);
                case null, default ->
                    // fallback to toString
                        jsonFactory().createString(String.valueOf(base));
            };
        } else if (v.isArray()) {
            IJsonArray a = v.asArray();
            IJsonArrayMutable out = jsonFactory().createArrayMutable();
            for (int i = 0; i < a.size(); i++) {
                out.add(deepCloneRecreateStrings(a.get_(i)));
            }
            return out;
        } else { // object
            IJsonObject o = v.asObject();
            IJsonObjectMutable out = jsonFactory().createObjectMutable();
            for (String k : o.keys()) {
                out.setProperty(k, deepCloneRecreateStrings(o.get_(k)));
            }
            return out;
        }
    }

    private void ensureNodes() {
        if (nodesArr == null) {
            nodesArr = jsonFactory().createArrayMutable();
            root.setProperty(NODES, nodesArr);
        }
    }

    private void ensureRelations() {
        if (relationsArr == null) {
            relationsArr = jsonFactory().createArrayMutable();
            root.setProperty(RELATIONS, relationsArr);
        }
    }

}
