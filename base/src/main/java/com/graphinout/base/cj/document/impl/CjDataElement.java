package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjDataMutable;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.json.util.JsonMaker;
import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;

import org.jspecify.annotations.Nullable;
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
    public void remove(String propertyKey) {
        if(root==null) {
            throw new IllegalStateException("Root is null");
        }
        root = JsonMaker.removeProperty(root, propertyKey);
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
