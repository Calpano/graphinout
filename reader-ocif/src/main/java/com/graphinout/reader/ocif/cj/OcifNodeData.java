package com.graphinout.reader.ocif.cj;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.IOcifNode;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.types.OcifAngle;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * Decorate an {@link IJsonObjectMutable} for OCIF node-level data stored as CJ node data
 */
public class OcifNodeData {

    private final IJsonObjectMutable o;

    public OcifNodeData(IJsonObjectMutable o) {this.o = o;}

    /**
     * Create CJ node data from OCIF node properties
     */
    public static OcifNodeData ofOcifNode(IOcifNode ocifNode) {
        OcifNodeData ocifNodeData = new OcifNodeData(factory().createObjectMutable());
        ifPresentAccept(ocifNode.position(), ocifNodeData::position);
        ifPresentAccept(ocifNode.size(), ocifNodeData::size);
        ifPresentAccept(ocifNode.rotation(), ocifNodeData::rotation);
        ifPresentAccept(ocifNode.resource(), ocifNodeData::resource);
        ifPresentAccept(ocifNode.resourceFit(), ocifNodeData::resourceFit);
        ifPresentAccept(ocifNode.relation(), ocifNodeData::relation);
        return ocifNodeData;
    }

    /**
     * Restore CJ data back into OCIF node properties
     */
    public static void toOcifNode(@Nullable IJsonValue jsonValue, IOcifNodeMutable ocifNode) {
        if (jsonValue == null) return;
        OcifNodeData d = new OcifNodeData(jsonValue.asObject().mutableCopy());
        ifPresentAccept(d.position(), ocifNode::position);
        ifPresentAccept(d.size(), ocifNode::size);
        ifPresentAccept(d.rotation(), OcifAngle::of, ocifNode::rotation);
        // map optional resource reference from CJ data if present
        ifPresentAccept(d.resource(), ocifNode::resource);
        ifPresentAccept(d.resourceFit(), ocifNode::resourceFit);
        // map optional back-reference to relation if present
        ifPresentAccept(d.relation(), ocifNode::relation);
    }

    public boolean isEmpty() {
        return o.isEmpty();
    }

    public IJsonObject jsonObject() {
        return o;
    }

    public void position(OcifVector23D vector23D) {
        o.add(OCIF.Node.POSITION, vector23D.toJson());
    }

    public OcifVector23D position() {
        return mapOrNull(o.get(OCIF.Node.POSITION), OcifVector23D::of);
    }

    public void relation(String relation) {
        o.add(OCIF.Node.RELATION, relation);
    }

    public String relation() {
        return o.getString(OCIF.Node.RELATION);
    }

    public void resource(String resource) {
        o.add(OCIF.Node.RESOURCE, resource);
    }

    public String resource() {
        return o.getString(OCIF.Node.RESOURCE);
    }

    public void resourceFit(IOcifNodeMutable.ResourceFit resourceFit) {
        o.add(OCIF.Node.RESOURCE_FIT, resourceFit.name());
    }

    public IOcifNodeMutable.ResourceFit resourceFit() {
        return mapOrNull(o.getString(OCIF.Node.RESOURCE_FIT), IOcifNodeMutable.ResourceFit::valueOf);
    }

    public void rotation(@NonNull OcifAngle rotation) {
        o.add(OCIF.Node.ROTATION, rotation.toJson());
    }

    public Double rotation() {
        return mapOrNull(o.get(OCIF.Node.ROTATION), j -> j.asNumber().doubleValue());
    }

    public void size(OcifVector23D vector23D) {
        o.add(OCIF.Node.SIZE, vector23D.toJson());
    }

    public OcifVector23D size() {
        return mapOrNull(o.get(OCIF.Node.SIZE), OcifVector23D::of);
    }


}
