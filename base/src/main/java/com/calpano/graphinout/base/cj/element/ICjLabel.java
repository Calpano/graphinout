package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjLabelElement;
import com.calpano.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import org.slf4j.Logger;

import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.util.Nullables.mapOrNull;
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
            _log.debug("Could not parse JSON from <<" + plainTextOrJson + ">>");
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

    default String toJsonString() {
        Json2StringWriter w = new Json2StringWriter();
        Cj2JsonWriter cjWriter = new Cj2JsonWriter(w);
        cjWriter.arrayStart();
        entries().forEach(x -> x.fire(cjWriter));
        cjWriter.arrayEnd();
        return w.jsonString();
    }

}
