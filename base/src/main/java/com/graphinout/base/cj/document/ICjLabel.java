package com.graphinout.base.cj.document;

import com.graphinout.base.cj.document.impl.CjLabelElement;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.impl.Json2JavaJsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import org.slf4j.Logger;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
import static org.slf4j.LoggerFactory.getLogger;

public interface ICjLabel extends ICjElement {

    Logger _log = getLogger(ICjLabel.class);

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

    @Override
    default Stream<ICjElement> directChildren() {
        return entries().map(x -> x);
    }

    Stream<ICjLabelEntry> entries();

    default Object toJaJsonMap() {
        if (entries().count() == 1) {
            ICjLabelEntry firstEntry = entries().findFirst().get();
            if (firstEntry.language() == null) {
                return firstEntry.value();
            }
        }
        return entries().map(ICjLabelEntry::toJaJsonMap).collect(Collectors.toList());
    }

    default String toJsonString() {
        Json2StringWriter w = new Json2StringWriter();
        Cj2JsonWriter cjWriter = new Cj2JsonWriter(w);
        cjWriter.arrayStart();
        entries().forEach(x -> x.fire(cjWriter));
        cjWriter.arrayEnd();
        return w.jsonString();
    }

}
