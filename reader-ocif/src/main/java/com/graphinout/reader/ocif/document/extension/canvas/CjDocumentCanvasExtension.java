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

import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
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

    public CjDocumentCanvasExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        CjDocumentCanvasExtension canvasExtension = new CjDocumentCanvasExtension();
        obj.forEach(canvasExtension::set);
        return canvasExtension;
    }

    public void baseUri(@NonNull String baseUri) {
        set(BASE_URI, baseUri);
    }

    public @Nullable String baseUri() {
        return mapOrNull(map(), m -> m.get(BASE_URI), IJsonValue::asString);
    }

    public ICjDocumentMetaMutable connectedJson() {
        return mapOrNull(map(), m -> m.get(CONNECTED_JSON), ICjDocumentMetaMutable::of);
    }

    public void connectedJson(@NonNull ICjDocumentMeta cjDocumentMeta) {
        assert cjDocumentMeta != null : "cjDocumentMeta cannot be null";
        set(CONNECTED_JSON, cjDocumentMeta.toJsonValue());
    }

    public CjDocumentCanvasExtension copy() {
        CjDocumentCanvasExtension data = new CjDocumentCanvasExtension();
        map().forEach(data::set);
        return data;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(BASE_URI, CONNECTED_JSON);
    }

    public boolean isEmpty() {
        return map().isEmpty();
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE,TYPE_NAME);
        ifPresentAccept(connectedJson(), ICjElement::toJsonValue, v->o.add(CONNECTED_JSON, v));
        ifPresentAccept(baseUri(),v->o.add(BASE_URI, v));
        return o;
    }

    @Override
    public String toString() {
        return "CjDocumentCanvasExtension{" + map().entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(joining(", ")) + "}";
    }

}
