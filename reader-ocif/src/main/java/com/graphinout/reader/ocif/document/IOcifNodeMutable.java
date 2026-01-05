package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif.document.types.OcifAngle;
import com.graphinout.reader.ocif.document.types.OcifVector23D;

public interface IOcifNodeMutable extends IOcifNode, IOcifItemMutable {

    IOcifNodeMutable addNodeExtension(IOcifNodeExtension ext);

    @Override
    IOcifNodeMutable id(String id);

    IOcifNodeMutable position(OcifVector23D position);

    IOcifNodeMutable relation(String relation);

    IOcifNodeMutable resource(String resource);

    IOcifNodeMutable resourceFit(ResourceFit resourceFit);

    IOcifNodeMutable rotation(OcifAngle rotation);

    IOcifNodeMutable size(OcifVector23D size);

}
