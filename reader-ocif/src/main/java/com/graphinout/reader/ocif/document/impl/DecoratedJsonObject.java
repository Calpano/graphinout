package com.graphinout.reader.ocif.document.impl;

import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.IDecorateJsonObjectMutable;

import java.util.Map;
import java.util.TreeMap;

public abstract class DecoratedJsonObject implements IDecorateJsonObjectMutable {

    private final Map<String, IJsonValue> map = new TreeMap<>();

    @Override
    public Map<String, IJsonValue> map() {
        return map;
    }

}
