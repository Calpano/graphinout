package com.graphinout.base.cj.document;

import com.graphinout.base.cj.document.impl.CjDocumentMetaElement;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

public interface ICjDocumentMeta extends ICjElement {

    static ICjDocumentMeta of(@NonNull IJsonValue jsonValue) {
        return ICjDocumentMetaMutable.of(jsonValue);
    }

    @Nullable Boolean canonical();

    default ICjDocumentMetaMutable copyMutable() {
        ICjDocumentMetaMutable copy = new CjDocumentMetaElement();
        copy.canonical(canonical());
        copy.versionDate(versionDate());
        copy.versionNumber(versionNumber());
        return copy;
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.empty();
    }

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap().putMaybe("canonical", canonical()).putMaybe("versionDate", versionDate()).putMaybe("versionNumber", versionNumber()).build();
    }

    @Nullable String versionDate();

    @Nullable String versionNumber();

}
