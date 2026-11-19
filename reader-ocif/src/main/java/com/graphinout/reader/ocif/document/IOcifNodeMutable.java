package com.graphinout.reader.ocif.document;

import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import org.jspecify.annotations.Nullable;
import com.graphinout.reader.ocif.document.types.OcifAngle;
import com.graphinout.reader.ocif.document.types.OcifVector23D;

public interface IOcifNodeMutable extends IOcifNode, IOcifItemMutable {

    IOcifNodeMutable addNodeExtension(IOcifNodeExtension ext);

    IOcifNodeMutable deleteWithParent(@Nullable Boolean deleteWithParent);

    @Override
    IOcifNodeMutable id(String id);

    IOcifNodeMutable parent(@Nullable String parent);

    IOcifNodeMutable position(@Nullable OcifVector23D position);

    IOcifNodeMutable relation(@Nullable String relation);

    IOcifNodeMutable resource(@Nullable String resource);

    IOcifNodeMutable resourceFit(@Nullable ResourceFit resourceFit);

    IOcifNodeMutable rotation(@Nullable OcifAngle rotation);

    IOcifNodeMutable rotationAxis(@Nullable OcifVector23D rotationAxis);

    IOcifNodeMutable scale(@Nullable OcifVector23D scale);

    IOcifNodeMutable size(@Nullable OcifVector23D size);

}
