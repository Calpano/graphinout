package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.cj.OcifCj;
import com.graphinout.reader.ocif.document.extension.canvas.IOcifCanvasExtension;
import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif.document.extension.relation.IOcifRelationExtension;
import com.graphinout.reader.ocif.document.extension.representation.IOcifRepresentationExtension;
import com.graphinout.reader.ocif.document.extension.resource.IOcifResourceExtension;
import org.jspecify.annotations.NonNull;

import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * TODO add to spec
 * <p>
 * A generic JSON data extension.
 */
public class DataExtension extends OcifExtension implements IOcifCanvasExtension, IOcifNodeExtension, IOcifRelationExtension, IOcifResourceExtension, IOcifRepresentationExtension {

    public static final String TYPE_NAME = "@ocif/data";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/data.json";

    public DataExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull DataExtension of(@NonNull IJsonObject obj) {
        DataExtension data = new DataExtension();
        obj.forEach(data::set);
        return data;
    }

    /**
     * OCIF Data Extension is always a JSON object (with one built-in property 'type');
     *
     * @param jsonValue
     * @return
     */
    public static DataExtension of(@NonNull IJsonValue jsonValue) {
        if (jsonValue.isObject()) {
            return of(jsonValue.asObject());
        } else {
            DataExtension ocifData = new DataExtension();
            ocifData.map().put(OcifCj.CjInOcifData.DATA_NON_OBJECT, jsonValue);
            return ocifData;
        }
    }

    public DataExtension copy() {
        DataExtension data = new DataExtension();
        map().forEach(data::set);
        return data;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of();
    }

    public boolean isEmpty() {
        return map().isEmpty();
    }

    @Override
    public String toString() {
        return "DataExtension{" + map().entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(joining(", ")) + "}";
    }

}
