package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.foundation.json.value.IAppendableJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.jackson.JacksonAppendableObject;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class CjDataElement extends CjWithDataElement implements ICjElement, IAppendableJsonObject {

    private JacksonAppendableObject dataObject = null;

    CjDataElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    @Override
    public IAppendableJsonObject addProperty(String key, IJsonValue jsonValue) {
        ensureDataObject();
        return dataObject.addProperty(key, jsonValue);
    }

    @Override
    public Object base() {
        ensureDataObject();
        return dataObject.base();
    }

    @Override
    public CjType cjType() {
        return CjType.Data;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.jsonDataStart();
        dataObject.fire(cjWriter);
        cjWriter.jsonDataEnd();
    }


    @Override
    public IJsonFactory factory() {
        ensureDataObject();
        return dataObject.factory();
    }

    @Nullable
    @Override
    public IJsonValue get(String key) {
        if (dataObject == null) return null;
        return dataObject.get(key);
    }

    @Override
    public Set<String> keys() {
        if (dataObject == null) return Collections.emptySet();
        return dataObject.keys();
    }

    @Override
    public int size() {
        if (dataObject == null) return 0;
        return dataObject.size();
    }

    private void ensureDataObject() {
        if (dataObject == null) {
            dataObject = new JacksonAppendableObject(new ObjectNode(JsonNodeFactory.instance));
        }
    }

}
