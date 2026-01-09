package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.collections.jajson.Json2JsonValueWriter;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public interface ICjLabelEntry extends ICjHasData {

    @Nullable
    ICjData data();

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.empty();
    }

    @Nullable
    String language();

    String value();

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap()
                .putMaybe(CjConstants.LANGUAGE, language())
                .putMaybe(CjConstants.VALUE, value())
                .build();
    }

    /**
     * Override to avoid double object wrapper
     * @return
     */
    @Override
    default IJsonObject toJsonValue() {
        Json2JsonValueWriter json2JsonValueWriter = new Json2JsonValueWriter(IJsonFactory.INSTANCE);
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2JsonValueWriter);
        fire(cj2JsonWriter);
        return Objects.requireNonNull(json2JsonValueWriter.resultJsonRootObject()).asObject();
    }


}
