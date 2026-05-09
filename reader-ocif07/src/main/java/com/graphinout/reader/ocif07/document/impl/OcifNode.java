package com.graphinout.reader.ocif07.document.impl;

import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.IOcifNodeMutable;
import com.graphinout.reader.ocif07.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif07.document.types.OcifAngle;
import com.graphinout.reader.ocif07.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OcifNode extends DecoratedJsonObject implements IOcifNodeMutable {

    private final List<IOcifNodeExtension> extensions = new ArrayList<>();
    private String id;
    private String parent;
    private Boolean deleteWithParent;
    /** (x,y) or (x,y,z) */
    private OcifVector23D position;
    /** (w,h[,d]) */
    private OcifVector23D size;
    private String resource;
    private ResourceFit resourceFit;
    private OcifAngle rotation;
    private OcifVector23D rotationAxis;
    private OcifVector23D scale;
    /** id of relation representing this node (rare) */
    private String relation;

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
    public Boolean deleteWithParent() {return deleteWithParent;}

    @Override
    public IOcifNodeMutable deleteWithParent(Boolean deleteWithParent) {
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
    public String parent() {return parent;}

    @Override
    public IOcifNodeMutable parent(String parent) {
        this.parent = parent;
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
    public OcifVector23D rotationAxis() {return rotationAxis;}

    @Override
    public IOcifNodeMutable rotationAxis(OcifVector23D rotationAxis) {
        this.rotationAxis = rotationAxis;
        return this;
    }

    @Override
    public OcifVector23D scale() {return scale;}

    @Override
    public IOcifNodeMutable scale(OcifVector23D scale) {
        this.scale = scale;
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
