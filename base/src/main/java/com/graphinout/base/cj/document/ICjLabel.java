package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.impl.CjLabelElement;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.impl.Json2JavaJsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
import static org.slf4j.LoggerFactory.getLogger;

public interface ICjLabel extends ICjElement, ICjHasData, Comparable<ICjLabel> {

    Logger _log = getLogger(ICjLabel.class);

    static ICjLabel fromJsonValue(IJsonValue jsonValue) {
        CjLabelElement cjLabelElement = new CjLabelElement();
        jsonValue.asArray().forEach(jsonEntry -> cjLabelElement.addEntry(cjEntry -> {
            IJsonObject jo = jsonEntry.asObject();
            ifPresentAccept(jo.get(CjConstants.VALUE), IJsonValue::asString, cjEntry::value);
            ifPresentAccept(jo.get(CjConstants.LANGUAGE), IJsonValue::asString, cjEntry::language);
        }));
        return cjLabelElement;
    }

    static ICjLabel fromPlainTextOrJson(String plainTextOrJson) {
        CjLabelElement cjLabelElement = new CjLabelElement();
        try {
            Json2JavaJsonWriter json2JavaJsonWriter = new Json2JavaJsonWriter();
            JsonReaderImpl jsonReader = new JsonReaderImpl();
            jsonReader.read(SingleInputSourceOfString.of("json", plainTextOrJson), json2JavaJsonWriter);
            IJsonValue jsonValue = json2JavaJsonWriter.jsonValue();
            jsonValue.asArray().forEach(jsonEntry -> {
                cjLabelElement.addEntry(cjEntry -> {
                    cjEntry.language(mapOrNull(jsonEntry.asObject().get("language"), IJsonValue::asString));
                    cjEntry.value(mapOrNull(jsonEntry.asObject().get("value"), IJsonValue::asString));
                });
            });
        } catch (Exception e) {
            //_log.debug("Could not parse JSON from <<" + plainTextOrJson + ">>, so interpreting as plain text");
            cjLabelElement.addEntry(cjEntry -> {
                cjEntry.value(plainTextOrJson);
            });
        }
        return cjLabelElement;
    }

    static IJsonArray toJsonValue(ICjLabel cjLabel) {
        return cjLabel.entries().map(ICjLabelEntry::toJsonValue).collect(IJsonFactory.INSTANCE.arrayCollector());
    }

    /**
     * Compare by entries, then data
     *
     * @param other
     * @return
     */
    default int compareTo(@NonNull ICjLabel other) {
        return Comparables.<ICjLabel>comparing().byStream(ICjLabel::entries, ICjLabelEntry::compareTo).compare(this, other);
    }

    default ICjLabelMutable copyMutable() {
        CjLabelElement copy = new CjLabelElement();
        copyTo(copy);
        return copy;
    }

    default void copyTo(ICjLabelMutable label) {
        entries().forEach(entry -> label.addEntry(entry::copyTo));
    }


    @Override
    default Stream<ICjElement> directChildren() {
        return entries().map(x -> x);
    }

    @NonNull Stream<ICjLabelEntry> entries();

    /**
     * @return the single graph (or null if none).
     * @throws IllegalStateException if multiple entries are present
     */
    default @Nullable ICjLabelEntry theEntry() throws IllegalStateException {
        List<ICjLabelEntry> entries = entries().toList();
        if (entries.isEmpty()) return null;
        if (entries.size() == 1) return entries.getFirst();
        throw new IllegalStateException("Multiple entries present, use entries() instead.");
    }

    default Object toJaJsonMap() {
        if (entries().count() == 1) {
            @SuppressWarnings("OptionalGetWithoutIsPresent") ICjLabelEntry firstEntry = entries().findFirst().get();
            if (firstEntry.language() == null) {
                return firstEntry.value();
            }
        }
        return entries().map(ICjLabelEntry::toJaJsonMap).collect(Collectors.toList());
    }

    default IJsonArray toJsonArrayOfEntries() {
        IJsonArrayMutable a = IJsonFactory.INSTANCE.createArrayMutable();
        entries().forEach(x -> a.add(x.toJsonValue()));
        return a;
    }

    default String toJsonString() {
        Json2StringWriter w = new Json2StringWriter();
        Cj2JsonWriter cjWriter = new Cj2JsonWriter(w);
        cjWriter.arrayStart();
        entries().forEach(x -> x.fire(cjWriter, true));
        cjWriter.arrayEnd();
        return w.jsonString();
    }


}
