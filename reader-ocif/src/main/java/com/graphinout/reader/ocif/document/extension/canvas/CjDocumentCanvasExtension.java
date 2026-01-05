package com.graphinout.reader.ocif.document.extension.canvas;

import com.graphinout.base.cj.document.ICjDocumentMeta;
import com.graphinout.base.cj.document.ICjDocumentMetaMutable;
import com.graphinout.base.cj.document.ICjElement;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif.Ocifs.factory;
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
    public static final String BASE_URI = "baseUri";
    private static final String CONNECTED_JSON = "connectedJson";
    private @Nullable String baseUri;
    private @Nullable ICjDocumentMeta connectedJson;


    public CjDocumentCanvasExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        CjDocumentCanvasExtension canvasExtension = new CjDocumentCanvasExtension();
        ifPresentAccept(obj.get(BASE_URI), IJsonValue::asString, canvasExtension::baseUri);
        ifPresentAccept(obj.get(CONNECTED_JSON), ICjDocumentMetaMutable::of, canvasExtension::connectedJson);
        return canvasExtension;
    }

    public void baseUri(@NonNull String baseUri) {
        this.baseUri = baseUri;
    }

    public @Nullable String baseUri() {
        return baseUri;
    }

    public ICjDocumentMeta connectedJson() {
        return connectedJson;
    }

    public void connectedJson(@NonNull ICjDocumentMeta cjDocumentMeta) {
        this.connectedJson = cjDocumentMeta;
    }

    public CjDocumentCanvasExtension copy() {
        CjDocumentCanvasExtension data = new CjDocumentCanvasExtension();
        ifPresentAccept(baseUri(), data::baseUri);
        ifPresentAccept(connectedJson(), data::connectedJson);
        return data;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(BASE_URI, CONNECTED_JSON);
    }

    public boolean isEmpty() {
        return map().isEmpty();
    }

    /** Immutable copy */
    public Map<String, Object> map() {
        Map<String, Object> map = new TreeMap<>();
        ifPresentAccept(baseUri(), v -> map.put(BASE_URI, v));
        ifPresentAccept(connectedJson(), ICjDocumentMeta::toJaJsonMap, v -> map.put(CONNECTED_JSON, v));
        return map;
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        ifPresentAccept(connectedJson(), ICjElement::toJsonValue, v -> o.add(CONNECTED_JSON, v));
        ifPresentAccept(baseUri(), v -> o.add(BASE_URI, v));
        return o;
    }

    @Override
    public String toString() {
        return "CjDocumentCanvasExtension{" + map().entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(joining(", ")) + "}";
    }

}
