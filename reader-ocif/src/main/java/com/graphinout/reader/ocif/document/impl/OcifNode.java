package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif.document.types.OcifAngle;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OcifNode extends DecoratedJsonObject implements IOcifNodeMutable {

    private final List<IOcifNodeExtension> extensions = new ArrayList<>();
    private String id;
    /** (x,y) or (x,y,z) */
    private OcifVector23D position;
    /** (w,h[,d]) */
    private OcifVector23D size;
    private String resource;
    private ResourceFit resourceFit;
    private OcifAngle rotation;
    /** id of relation representing this node (rare) */
    private String relation;

    @Override
    public IOcifNodeMutable addNodeExtension(IOcifNodeExtension ext) {
        if (ext != null) this.extensions.add(ext);
        return this;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Node.ID, OCIF.Node.POSITION, OCIF.Node.SIZE, OCIF.Node.RESOURCE, OCIF.Node.RESOURCE_FIT, OCIF.Node.ROTATION, OCIF.Node.RELATION);
    }

    @Override
    public @NonNull List<IOcifNodeExtension> extensions() {return extensions;}

    @Override
    public String id() {return id;}

    @Override
    public IOcifNodeMutable id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public OcifVector23D position() {return position;}

    @Override
    public IOcifNodeMutable position(OcifVector23D position) {
        this.position = position;
        return this;
    }

    @Override
    public String relation() {return relation;}

    @Override
    public IOcifNodeMutable relation(String relation) {
        this.relation = relation;
        return this;
    }

    @Override
    public String resource() {return resource;}

    @Override
    public IOcifNodeMutable resource(String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public ResourceFit resourceFit() {return resourceFit;}

    @Override
    public IOcifNodeMutable resourceFit(ResourceFit resourceFit) {
        this.resourceFit = resourceFit;
        return this;
    }

    @Override
    public OcifAngle rotation() {return rotation;}

    @Override
    public IOcifNodeMutable rotation(OcifAngle rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public IOcifNodeMutable size(OcifVector23D size) {
        this.size = size;
        return this;
    }

    @Override
    public OcifVector23D size() {return size;}

}
