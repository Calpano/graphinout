package com.calpano.graphinout.foundation.json.value.java;

import com.calpano.graphinout.foundation.json.value.IAppendableJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.util.ArrayList;
import java.util.List;

public class JavaJsonAppendableArray implements IAppendableJsonArray {

    private final List<IJsonValue> list = new ArrayList<>();

    public static IAppendableJsonArray of(List<IJsonValue> list) {
        return new JavaJsonAppendableArray().addAll(list);
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
    public int size() {
        return list.size();
    }

    private IAppendableJsonArray addAll(List<IJsonValue> list) {
        this.list.addAll(list);
        return this;
    }

}
