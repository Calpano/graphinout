package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class OcifNode implements IOcifNodeMutable {

    private final List<IOcifExtension> extensions = new ArrayList<>();
    private String id;
    /** (x,y) or (x,y,z) */
    private OcifVector23D position;
    /** (w,h[,d]) */
    private OcifVector23D size;
    private String resource;
    private ResourceFit resourceFit;
    private Double rotation;
    /** id of relation representing this node (rare) */
    private String relation;

    @Override
    public IOcifNodeMutable addExtension(IOcifExtension ext) {
        if (ext != null) this.extensions.add(ext);
        return this;
    }

    @Override
    public @NonNull List<IOcifExtension> extensions() {return extensions;}

    @Override
    public String id() {return id;}

    @Override
    public OcifVector23D position() {return position;}

    @Override
    public String relation() {return relation;}

    @Override
    public String resource() {return resource;}

    @Override
    public ResourceFit resourceFit() {return resourceFit;}

    @Override
    public Double rotation() {return rotation;}

    @Override
    public IOcifNodeMutable setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public IOcifNodeMutable setPosition(OcifVector23D position) {
        this.position = position;
        return this;
    }

    @Override
    public IOcifNodeMutable setRelation(String relation) {
        this.relation = relation;
        return this;
    }

    @Override
    public IOcifNodeMutable setResource(String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public IOcifNodeMutable setResourceFit(ResourceFit resourceFit) {
        this.resourceFit = resourceFit;
        return this;
    }

    @Override
    public IOcifNodeMutable setRotation(Double rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public IOcifNodeMutable setSize(OcifVector23D size) {
        this.size = size;
        return this;
    }

    @Override
    public OcifVector23D size() {return size;}

}
