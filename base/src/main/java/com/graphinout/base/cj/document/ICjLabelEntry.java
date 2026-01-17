package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.impl.CjLabelEntryElement;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.collections.jajson.Json2JsonValueWriter;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

public interface ICjLabelEntry extends ICjHasData {

    default ICjLabelEntryMutable copyMutable() {
        CjLabelEntryElement copy = new CjLabelEntryElement();
        copyTo(copy);
        return copy;
    }

    default void copyTo(ICjLabelEntryMutable entry) {
        entry.value(value());
        ifPresentAccept(language(), entry::language);
        ifPresentAccept(data(), data -> entry.dataMutable(d -> d.setJsonValue(data.jsonValue())));
    }


    @Nullable
    ICjData data();

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.empty();
    }

    @Nullable
    String language();

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap()
                .putMaybe(CjConstants.LANGUAGE, language())
                .putMaybe(CjConstants.VALUE, value())
                .build();
    }

    /**
     * Override to avoid double object wrapper
     */
    @Override
    default IJsonObject toJsonValue() {
        Json2JsonValueWriter json2JsonValueWriter = new Json2JsonValueWriter(IJsonFactory.INSTANCE);
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2JsonValueWriter);
        fire(cj2JsonWriter);
        return Objects.requireNonNull(json2JsonValueWriter.resultJsonRootObject()).asObject();
    }

    String value();


}
