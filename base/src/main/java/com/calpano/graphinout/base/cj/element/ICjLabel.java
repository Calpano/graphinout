package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjLabelElement;
import com.calpano.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.io.IOException;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.util.Nullables.mapOrNull;

public interface ICjLabel extends ICjElement {

    static ICjLabel fromJson(String json) {
        Json2JavaJsonWriter json2JavaJsonWriter = new Json2JavaJsonWriter();
        try {
            JsonReaderImpl jsonReader = new JsonReaderImpl();
            jsonReader.read(SingleInputSourceOfString.of("json", json), json2JavaJsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        IJsonValue jsonValue = json2JavaJsonWriter.jsonValue();
        CjLabelElement cjLabelElement = new CjLabelElement();
        jsonValue.asArray().forEach(jsonEntry -> {
            cjLabelElement.addEntry(cjEntry -> {
                cjEntry.language(mapOrNull(jsonEntry.asObject().get("language"), IJsonValue::asString));
                cjEntry.value(mapOrNull(jsonEntry.asObject().get("value"), IJsonValue::asString));
            });
        });
        return cjLabelElement;
    }

    Stream<ICjLabelEntry> entries();

    default String toJsonString() {
        Json2StringWriter w = new Json2StringWriter();
        Cj2JsonWriter cjWriter = new Cj2JsonWriter(w);
        cjWriter.arrayStart();
        entries().forEach(x -> x.fire(cjWriter));
        cjWriter.arrayEnd();
        return w.jsonString();
    }

}
