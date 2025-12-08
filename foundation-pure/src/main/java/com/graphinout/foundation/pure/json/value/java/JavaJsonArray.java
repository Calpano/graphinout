package com.graphinout.foundation.pure.json.value.java;

import com.graphinout.foundation.pure.json.document.IJsonArrayAppendable;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;

import java.util.ArrayList;
import java.util.List;

public class JavaJsonArray implements IJsonArrayMutable {

    private final List<IJsonValue> list = new ArrayList<>();

    public static IJsonArrayAppendable of(List<IJsonValue> list) {
        return new JavaJsonArray().addAll(list);
    }

    @Override
    public void add(IJsonValue jsonValue) {
        list.add(jsonValue);
    }

    @Override
    public Object base() {
        return list;
    }

    @Override
    public IJsonFactory factory() {
        return JavaJsonFactory.INSTANCE;
    }

    @Override
    public IJsonValue get(int index) throws IndexOutOfBoundsException {
        return list.get(index);
    }

    @Override
    public void remove(int index) throws ArrayIndexOutOfBoundsException {
        list.remove(index);
    }

    @Override
    public void set(int index, IJsonValue jsonValue) throws ArrayIndexOutOfBoundsException {
        list.set(index, jsonValue);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public String toString() {
        Json2StringWriter w = new Json2StringWriter();
        fire(w);
        return w.jsonString();
    }

    private IJsonArrayAppendable addAll(List<IJsonValue> list) {
        this.list.addAll(list);
        return this;
    }

}
