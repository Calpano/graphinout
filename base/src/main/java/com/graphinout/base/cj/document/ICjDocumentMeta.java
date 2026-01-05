package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

public interface ICjDocumentMeta extends ICjElement {

    @Nullable Boolean canonical();

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
