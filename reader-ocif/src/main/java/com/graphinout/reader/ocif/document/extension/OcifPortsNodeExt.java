package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;

/**
 * Ports Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Ports Node Extension"
 * Name: @ocif/node/ports
 * URI: https://spec.canvasprotocol.org/v0.6/extensions/ports-node.json
 * <p>
 * Properties:
 * - ports (array of ID strings, required): IDs of nodes acting as ports for the host node
 * </p>
 */
public class OcifPortsNodeExt implements IOcifExtension {
    public static final String TYPE = "@ocif/node/ports";
    public static final String URI = "https://spec.canvasprotocol.org/v0.6/extensions/ports-node.json";

    private IJsonArray ports; // array of string IDs
    private IJsonObject extras;

    @Override public String typeName() { return TYPE; }
    @Override public String typeUri() { return URI; }

    public IJsonArray getPorts() { return ports; }
    public OcifPortsNodeExt setPorts(IJsonArray ports) { this.ports = ports; return this; }

    public IJsonObject getExtras() { return extras; }
    public OcifPortsNodeExt setExtras(IJsonObject extras) { this.extras = extras; return this; }
}
