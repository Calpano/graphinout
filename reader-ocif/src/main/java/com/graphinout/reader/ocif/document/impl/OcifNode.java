package com.graphinout.reader.ocif.document.impl;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;

import java.util.ArrayList;
import java.util.List;

public class OcifNode implements IOcifNodeMutable {

    private final List<IOcifExtension> extensions = new ArrayList<>();
    private String id;
    /** (x,y) or (x,y,z) */
    private double[] position;
    /** (w,h[,d]) */
    private double[] size;
    private String resource;
    private ResourceFit resourceFit;
    private IJsonArray data;
    private Double rotation;
    /** id of relation representing this node (rare) */
    private String relation;
    /** extension/unknown properties */
    private IJsonObject extras;

    @Override
    public IOcifNodeMutable addExtension(IOcifExtension ext) {
        if (ext != null) this.extensions.add(ext);
        return this;
    }

    @Override
    public IJsonArray data() {return data;}

    @Override
    public IOcifNodeMutable setData(IJsonArray data) {
        this.data = data;
        return this;
    }

    @Override
    public List<IOcifExtension> extensions() {return extensions;}

    @Override
    public IJsonObject extras() {return extras;}

    @Override
    public IOcifNodeMutable setExtras(IJsonObject extras) {
        this.extras = extras;
        return this;
    }

    @Override
    public String id() {return id;}

    @Override
    public IOcifNodeMutable setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public double[] position() {return position;}

    @Override
    public IOcifNodeMutable setPosition(double[] position) {
        this.position = position;
        return this;
    }

    @Override
    public String relation() {return relation;}

    @Override
    public IOcifNodeMutable setRelation(String relation) {
        this.relation = relation;
        return this;
    }

    @Override
    public String resource() {return resource;}

    @Override
    public IOcifNodeMutable setResource(String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public ResourceFit resourceFit() {return resourceFit;}

    @Override
    public IOcifNodeMutable setResourceFit(ResourceFit resourceFit) {
        this.resourceFit = resourceFit;
        return this;
    }

    @Override
    public Double rotation() {return rotation;}

    @Override
    public IOcifNodeMutable setRotation(Double rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public double[] size() {return size;}

    @Override
    public IOcifNodeMutable setSize(double[] size) {
        this.size = size;
        return this;
    }

}
