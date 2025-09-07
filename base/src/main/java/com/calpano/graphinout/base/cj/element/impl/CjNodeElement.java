package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjNodeProperties;

import javax.annotation.Nullable;

public class CjNodeElement extends CjWithDataElement implements ICjNodeProperties, ICjElement {

    private String id;

    CjNodeElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    @Override
    public CjType cjType() {
        return CjType.Node;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.nodeStart();
        cjWriter.id(id);
        // including 'data'
        fireDataMaybe(cjWriter);
        cjWriter.nodeEnd();
    }

    @Override
    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
