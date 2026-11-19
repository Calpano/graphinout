package com.graphinout.reader.ocif.document.impl;

import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.Nullable;
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
    private @Nullable String parent;
    private @Nullable Boolean deleteWithParent;
    /** (x,y) or (x,y,z) */
    private @Nullable OcifVector23D position;
    /** (w,h[,d]) */
    private @Nullable OcifVector23D size;
    private @Nullable String resource;
    private @Nullable ResourceFit resourceFit;
    private @Nullable OcifAngle rotation;
    private @Nullable OcifVector23D rotationAxis;
    private @Nullable OcifVector23D scale;
    /** id of relation representing this node (rare) */
    private @Nullable String relation;

    @Override
    public IOcifNodeMutable addNodeExtension(IOcifNodeExtension ext) {
        if (ext != null) this.extensions.add(ext);
        return this;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Node.ID, OCIF.Node.PARENT, OCIF.Node.DELETE_WITH_PARENT, OCIF.Node.POSITION, OCIF.Node.SIZE, OCIF.Node.RESOURCE, OCIF.Node.RESOURCE_FIT, OCIF.Node.ROTATION, OCIF.Node.ROTATION_AXIS, OCIF.Node.SCALE, OCIF.Node.RELATION);
    }

    @Override
    public @NonNull List<IOcifNodeExtension> extensions() {return extensions;}

    @Override
    public @Nullable Boolean deleteWithParent() {return deleteWithParent;}

    @Override
    public IOcifNodeMutable deleteWithParent(@Nullable Boolean deleteWithParent) {
        this.deleteWithParent = deleteWithParent;
        return this;
    }

    @Override
    public String id() {return id;}

    @Override
    public IOcifNodeMutable id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public @Nullable String parent() {return parent;}

    @Override
    public IOcifNodeMutable parent(@Nullable String parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public @Nullable OcifVector23D position() {return position;}

    @Override
    public IOcifNodeMutable position(@Nullable OcifVector23D position) {
        this.position = position;
        return this;
    }

    @Override
    public @Nullable String relation() {return relation;}

    @Override
    public IOcifNodeMutable relation(@Nullable String relation) {
        this.relation = relation;
        return this;
    }

    @Override
    public @Nullable String resource() {return resource;}

    @Override
    public IOcifNodeMutable resource(@Nullable String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public @Nullable ResourceFit resourceFit() {return resourceFit;}

    @Override
    public IOcifNodeMutable resourceFit(@Nullable ResourceFit resourceFit) {
        this.resourceFit = resourceFit;
        return this;
    }

    @Override
    public @Nullable OcifAngle rotation() {return rotation;}

    @Override
    public IOcifNodeMutable rotation(@Nullable OcifAngle rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public @Nullable OcifVector23D rotationAxis() {return rotationAxis;}

    @Override
    public IOcifNodeMutable rotationAxis(@Nullable OcifVector23D rotationAxis) {
        this.rotationAxis = rotationAxis;
        return this;
    }

    @Override
    public @Nullable OcifVector23D scale() {return scale;}

    @Override
    public IOcifNodeMutable scale(@Nullable OcifVector23D scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public IOcifNodeMutable size(@Nullable OcifVector23D size) {
        this.size = size;
        return this;
    }

    @Override
    public @Nullable OcifVector23D size() {return size;}

}
