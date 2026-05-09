package com.graphinout.reader.ocif07.document;

import com.graphinout.reader.ocif07.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif07.document.types.OcifAngle;
import com.graphinout.reader.ocif07.document.types.OcifVector23D;

public interface IOcifNodeMutable extends IOcifNode, IOcifItemMutable {

    IOcifNodeMutable addNodeExtension(IOcifNodeExtension ext);

    IOcifNodeMutable deleteWithParent(Boolean deleteWithParent);

    @Override
    IOcifNodeMutable id(String id);

    IOcifNodeMutable parent(String parent);

    IOcifNodeMutable position(OcifVector23D position);

    IOcifNodeMutable relation(String relation);

    IOcifNodeMutable resource(String resource);

    IOcifNodeMutable resourceFit(ResourceFit resourceFit);

    IOcifNodeMutable rotation(OcifAngle rotation);

    IOcifNodeMutable rotationAxis(OcifVector23D rotationAxis);

    IOcifNodeMutable scale(OcifVector23D scale);

    IOcifNodeMutable size(OcifVector23D size);

}
