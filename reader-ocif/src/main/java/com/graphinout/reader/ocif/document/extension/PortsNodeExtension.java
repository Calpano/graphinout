package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Ports Node Extension.
 * <p>
 * Spec: reader-ocif/src/spec-v0.6/spec.md → "Node Extensions → Ports Node Extension" Name: @ocif/node/ports TYPE_URI:
 * https://spec.canvasprotocol.org/v0.6/extensions/ports-node.json
 * <p>
 * Properties:
 * <li>ports (array of ID strings, required): IDs of nodes acting as ports for the host node
 * </p>
 */
public class PortsNodeExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/node/ports";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/ports-node.json";
    /** array of string IDs */
    private IJsonArray ports;
    private IJsonObject extras;
    public PortsNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        PortsNodeExtension ext = new PortsNodeExtension();
        obj.getMaybeAs(OCIF.Common.PORTS, IJsonValue::asArray, ext::setPorts);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.PORTS);
    }

    public IJsonObject extras() {return extras;}

    public IJsonArray ports() {return ports;}

    public PortsNodeExtension setExtras(IJsonObject extras) {
        this.extras = extras;
        return this;
    }

    public PortsNodeExtension setPorts(IJsonArray ports) {
        this.ports = ports;
        return this;
    }

}
