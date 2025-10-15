package com.graphinout.base.cj.element.impl;

import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.element.ICjDataMutable;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.json.impl.JsonMaker;
import com.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;

import javax.annotation.Nullable;
import java.util.List;

public class CjDataElement implements ICjDataMutable {

    private IJsonValue root = null;
    /** IMPROVE configurable ? */
    private static final IJsonFactory factory = JavaJsonFactory.INSTANCE;

    @Override
    public void add(List<IJsonContainerNavigationStep> path, IJsonValue jsonValue) {
        assert jsonValue != null;
        this.root = JsonMaker.append(factory(), this.root, path, jsonValue);
    }

    @Override
    public CjType cjType() {
        return CjType.Data;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        if (root == null)
            return;

        cjWriter.jsonDataStart();
        root.fire(cjWriter);
        cjWriter.jsonDataEnd();
    }


    @Nullable
    @Override
    public IJsonValue jsonValue() {
        return root;
    }

    @Override
    public IJsonFactory factory() {
        return factory;
    }

}
