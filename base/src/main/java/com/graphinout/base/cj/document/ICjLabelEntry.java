package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.impl.CjLabelEntryElement;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.collections.jajson.Json2JsonValueWriter;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

public interface ICjLabelEntry extends ICjHasData, ICjElement, Comparable<ICjLabelEntry> {

    /**
     * Compare by value, language, data
     */
    default int compareTo(@NonNull ICjLabelEntry other) {
        return Comparables.<ICjLabelEntry>comparing() //
                .byKey(ICjLabelEntry::value)//
                .byKey(ICjLabelEntry::language)//
                .byKey(ICjHasData::data)//
                .compare(this, other);
    }

    default ICjLabelEntryMutable copyMutable() {
        CjLabelEntryElement copy = new CjLabelEntryElement();
        copyTo(copy);
        return copy;
    }

    default void copyTo(ICjLabelEntryMutable entry) {
        entry.value(value());
        ifPresentAccept(language(), entry::language);
        ifPresentAccept(data().jsonValue(), jsonValue -> entry.dataMutable(d -> d.setJsonValue(jsonValue)));
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.empty();
    }

    @Nullable String language();

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap().putMaybe(CjConstants.LANGUAGE, language()).putMaybe(CjConstants.VALUE, value()).build();
    }

    /**
     * Override to avoid double object wrapper
     */
    @Override
    default IJsonObject toJsonValue() {
        Json2JsonValueWriter json2JsonValueWriter = new Json2JsonValueWriter(IJsonFactory.INSTANCE);
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2JsonValueWriter);
        fire(cj2JsonWriter, true);
        return Objects.requireNonNull(json2JsonValueWriter.resultJsonRootObject()).asObject();
    }

    String value();

    /**
     * A hash based on value, language, and data.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder("V:").append(value());
        if (language() != null) {
            sb.append("|L:").append(language());
        }
        if (data() != null && !data().isEmpty()) {
            sb.append("|D:").append(data().hashCode());
        }
        return Integer.toString(sb.toString().hashCode());
    }

}
