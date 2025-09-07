package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjPortProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CjPortElement extends CjWithDataElement implements ICjPortProperties, ICjElement {

    private final List<CjPortElement> ports = new ArrayList<>();

    private String id;

    CjPortElement(@Nullable CjWithDataElement parent) {
        super(parent);
    }

    @Override
    public CjType cjType() {
        return CjType.Port;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.portStart();
        cjWriter.id(id);
        fireDataMaybe(cjWriter);
        cjWriter.portEnd();
    }

    public List<CjPortElement> getPorts() {
        return ports;
    }

    @Override
    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPorts(List<CjPortElement> ports) {
        this.ports.clear();
        this.ports.addAll(ports);
    }

}
