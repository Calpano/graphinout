package com.graphinout.reader.ocif.document.extension.node;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.graphinout.reader.ocif.Ocifs.factory;

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
public class PortsNodeExtension extends OcifExtension implements IOcifNodeExtension {

    public static final String PORTS = "ports";

    public static final String TYPE_NAME = "@ocif/node/ports";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/ports-node.json";
    /** array of string IDs */
    private List<String> ports = new ArrayList<>();

    public PortsNodeExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        PortsNodeExtension ext = new PortsNodeExtension();
        obj.getMaybeAs(OCIF.Common.PORTS, IJsonValue::asArray, a -> {
            ArrayList<String> a2 = new ArrayList<>();
            a.forEach(s -> a2.add(s.asString()));
            return a2;
        }, ext::ports);
        return ext;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.PORTS);
    }

    public PortsNodeExtension ports(List<String> ports) {
        this.ports = ports;
        return this;
    }

    public List<String> ports() {return ports;}

    public Map<String, Object> toMap() {
        return JaJson.createMap()//
                .putNonNull(TYPE, typeUri())//
                .putMaybe(OCIF.Common.PORTS, ports()).build();
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        if (!ports.isEmpty()) {
            o.addArray(OCIF.Common.PORTS, arr -> ports.forEach(arr::add));
        }
        return o;
    }

}
