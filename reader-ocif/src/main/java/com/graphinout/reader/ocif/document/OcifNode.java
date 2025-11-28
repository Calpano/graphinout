package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * OCIF Node.
 * <p>
 * Spec excerpts (schema.json $defs.node):
 * <ul>
 *   <li>id (string, required): A unique identifier for the node.</li>
 *   <li>position (array of number): Coordinate as (x,y) or (x,y,z).</li>
 *   <li>size (array of number): The size of the node per dimension.</li>
 *   <li>resource (string): The resource to display.</li>
 *   <li>resourceFit (enum): Fitting resource in item; one of none, containX, containY, contain, cover, fill, tile.</li>
 *   <li>data (array): Extended node data.</li>
 *   <li>rotation (number): +/- 360 degrees.</li>
 *   <li>relation (string): ID of a relation.</li>
 * </ul>
 * The spec.md section "Nodes" describes the semantics in more detail.
 */
public class OcifNode {

    public enum ResourceFit { none, containX, containY, contain, cover, fill, tile }

    private String id;
    private double[] position; // (x,y) or (x,y,z)
    private double[] size;     // (w,h[,d])
    private String resource;
    private ResourceFit resourceFit;
    private IJsonArray data;
    private Double rotation;
    private String relation; // id of relation representing this node (rare)
    private IJsonObject extras; // extension/unknown properties
    private final List<IOcifExtension> extensions = new ArrayList<>();

    public String getId() { return id; }
    public OcifNode setId(String id) { this.id = id; return this; }

    public double[] getPosition() { return position; }
    public OcifNode setPosition(double[] position) { this.position = position; return this; }

    public double[] getSize() { return size; }
    public OcifNode setSize(double[] size) { this.size = size; return this; }

    public String getResource() { return resource; }
    public OcifNode setResource(String resource) { this.resource = resource; return this; }

    public ResourceFit getResourceFit() { return resourceFit; }
    public OcifNode setResourceFit(ResourceFit resourceFit) { this.resourceFit = resourceFit; return this; }

    public IJsonArray getData() { return data; }
    public OcifNode setData(IJsonArray data) { this.data = data; return this; }

    public Double getRotation() { return rotation; }
    public OcifNode setRotation(Double rotation) { this.rotation = rotation; return this; }

    public String getRelation() { return relation; }
    public OcifNode setRelation(String relation) { this.relation = relation; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifNode setExtras(IJsonObject extras) { this.extras = extras; return this; }

    /** Typed extensions parsed from the node's data array. */
    public List<IOcifExtension> getExtensions() { return extensions; }
    public OcifNode addExtension(IOcifExtension ext) { if (ext != null) this.extensions.add(ext); return this; }
}
