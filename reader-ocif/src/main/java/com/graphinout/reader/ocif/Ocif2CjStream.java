package com.graphinout.reader.ocif;

import com.graphinout.base.cj.CjFactory;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjData;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonArrayMutable;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonPrimitive;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.graphinout.foundation.util.Nullables.ifConsumerPresentAccept;
import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

public class Ocif2CjStream extends CjFactory implements ICjStream {

    private final @Nullable Consumer<String> onDone;

    // Building state for CJ→OCIF
    private IJsonFactory factory;
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
            root.setProperty("relations", factory.createArrayMutable());
        }
        ifConsumerPresentAccept(onDone, resultOcifJsonString());
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // Initialize factory from document data factory to stay consistent
        ICjData docDataHolder = document.data();
        this.factory = (docDataHolder != null) ? docDataHolder.factory() : JavaJsonFactory.INSTANCE;
        this.root = factory.createObjectMutable();
        // Restore document-level OCIF info from data.ocif.* if present
        IJsonValue docData = docDataHolder == null ? null : docDataHolder.jsonValue();
        if (docData != null && docData.isObject()) {
            IJsonObject od = docData.asObject();
            if (od.hasProperty("ocif")) {
                IJsonValue ocifVal = od.get("ocif");
                if (ocifVal != null && ocifVal.isObject()) {
                    IJsonObject oco = ocifVal.asObject();
                    ifPresentAccept(oco.get("schemaUri"), v -> root.setProperty("ocif", v));
                    ifPresentAccept(oco.get("resources"), v -> root.setProperty("resources", deepCloneRecreateStrings(v)));
                    ifPresentAccept(oco.get("schemas"), v -> root.setProperty("schemas", v));
                    ifPresentAccept(oco.get("extra"), v -> (IJsonObject) v.asObject(), extras -> extras.keys().forEach(k -> root.setProperty(k, extras.get(k))));
                    // read flags
                    if (oco.hasProperty("flags")) {
                        IJsonObject flags = oco.get("flags").asObject();
                        if (flags.hasProperty("hasRelationsProperty") && flags.get("hasRelationsProperty").asBoolean()) {
                            this.hasRelationsProperty = true;
                        }
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
        ICjData edgeDataHolder = edge.data();
        IJsonValue v = edgeDataHolder == null ? null : edgeDataHolder.jsonValue();
        if (v != null && v.isObject()) {
            IJsonObject eo = v.asObject();
            if (eo.hasProperty("ocif")) {
                IJsonValue ocifVal = eo.get("ocif");
                if (ocifVal != null && ocifVal.isObject()) {
                    IJsonObject ocif = ocifVal.asObject();
                    if (ocif.hasProperty("relation")) {
                        relationsArr.add(ocif.get("relation"));
                        return;
                    }
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
        IJsonObjectMutable no = factory.createObjectMutable();
        String nodeId = node.id();
        if (nodeId != null) {
            no.setProperty("id", factory.createString(nodeId));
        }
        // Restore OCIF node fields from data.ocif.node.*
        ICjData nodeDataHolder = node.data();
        IJsonValue dv = nodeDataHolder == null ? null : nodeDataHolder.jsonValue();
        if (dv != null && dv.isObject()) {
            IJsonObject od = dv.asObject();
            if (od.hasProperty("ocif")) {
                IJsonValue ocifVal = od.get("ocif");
                if (ocifVal != null && ocifVal.isObject()) {
                    IJsonObject ocif = ocifVal.asObject();
                    if (ocif.hasProperty("node")) {
                        IJsonValue nodeVal = ocif.get("node");
                        if (nodeVal != null && nodeVal.isObject()) {
                            IJsonObject n = nodeVal.asObject();
                            if (n.hasProperty("position")) no.setProperty("position", n.get("position"));
                            if (n.hasProperty("size")) no.setProperty("size", n.get("size"));
                            if (n.hasProperty("resource")) no.setProperty("resource", n.get("resource"));
                            if (n.hasProperty("type")) no.setProperty("type", n.get("type"));
                            if (n.hasProperty("data")) no.setProperty("data", n.get("data"));
                            if (n.hasProperty("extra")) {
                                IJsonObject extras = n.get("extra").asObject();
                                extras.keys().forEach(k -> no.setProperty(k, extras.get(k)));
                            }
                        }
                    }
                }
            }
        }
        nodesArr.add(no);
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
        if (v == null) return factory.createNull();
        if (v.isPrimitive()) {
            IJsonPrimitive p = v.asPrimitive();
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
            IJsonArray a = v.asArray();
            IJsonArrayMutable out = factory.createArrayMutable();
            for (int i = 0; i < a.size(); i++) {
                out.add(deepCloneRecreateStrings(a.get_(i)));
            }
            return out;
        } else { // object
            IJsonObject o = v.asObject();
            IJsonObjectMutable out = factory.createObjectMutable();
            for (String k : o.keys()) {
                out.setProperty(k, deepCloneRecreateStrings(o.get_(k)));
            }
            return out;
        }
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

}
