package com.graphinout.reader.ocif07.document.extension.canvas;

import com.graphinout.base.cj.document.ICjDocumentMeta;
import com.graphinout.base.cj.document.ICjDocumentMetaMutable;
import com.graphinout.base.cj.document.ICjElement;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.reader.ocif07.document.extension.IOcifExtension;
import com.graphinout.reader.ocif07.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif07.Ocifs.factory;
import static java.util.stream.Collectors.joining;

/**
 * TODO add to CJ docs, maybe link from OCIF docs as example
 * <p>
 * An OCIF canvas extension to represent Connected JSON (CJ) document-level properties.
 * See <a href="https://calpano.github.io/connected-json/spec-cj.html#document">CJ spec</a>.
 */
public class CjDocumentCanvasExtension extends OcifExtension implements IOcifCanvasExtension {

    public static final String TYPE_NAME = "@connected-json/document";
    public static final String TYPE_URI = "https://j-s-o-n.org/ocif-doc/schema.json";
    public static final String CONTEXT = "@context";
    private static final String CONNECTED_JSON = "connectedJson";
    private @Nullable Map<String, String> context;
    private @Nullable ICjDocumentMeta connectedJson;


    public CjDocumentCanvasExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        CjDocumentCanvasExtension canvasExtension = new CjDocumentCanvasExtension();
        ifPresentAccept(obj.get(CONTEXT), contextVal -> {
            if (contextVal.isObject()) {
                Map<String, String> ctx = new LinkedHashMap<>();
                contextVal.asObject().forEach((k, v) -> ctx.put(k, v.asString()));
                canvasExtension.context(ctx);
            }
        });
        ifPresentAccept(obj.get(CONNECTED_JSON), ICjDocumentMetaMutable::of, canvasExtension::connectedJson);
        return canvasExtension;
    }

    public void context(@NonNull Map<String, String> context) {
        this.context = context;
    }

    public @Nullable Map<String, String> context() {
        return context;
    }

    public ICjDocumentMeta connectedJson() {
        return connectedJson;
    }

    public void connectedJson(@NonNull ICjDocumentMeta cjDocumentMeta) {
        this.connectedJson = cjDocumentMeta;
    }

    public CjDocumentCanvasExtension copy() {
        CjDocumentCanvasExtension data = new CjDocumentCanvasExtension();
        ifPresentAccept(context(), data::context);
        ifPresentAccept(connectedJson(), data::connectedJson);
        return data;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(CONTEXT, CONNECTED_JSON);
    }

    public boolean isEmpty() {
        return map().isEmpty();
    }

    /** Immutable copy */
    public Map<String, Object> map() {
        Map<String, Object> map = new TreeMap<>();
        ifPresentAccept(context(), v -> map.put(CONTEXT, v));
        ifPresentAccept(connectedJson(), ICjDocumentMeta::toJaJsonMap, v -> map.put(CONNECTED_JSON, v));
        return map;
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(connectedJson(), ICjElement::toJsonValue, v -> o.add(CONNECTED_JSON, v));
        ifPresentAccept(context(), ctx -> {
            IJsonObjectMutable ctxObj = factory().createObjectMutable();
            ctx.forEach(ctxObj::setString);
            o.add(CONTEXT, ctxObj);
        });
        return o;
    }

    @Override
    public String toString() {
        return "CjDocumentCanvasExtension{" + map().entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(joining(", ")) + "}";
    }

}
