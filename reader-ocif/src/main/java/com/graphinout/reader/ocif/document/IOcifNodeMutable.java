package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.types.OcifVector23D;

public interface IOcifNodeMutable extends IOcifNode {

    IOcifNodeMutable addExtension(IOcifExtension ext);

    IOcifNodeMutable setId(String id);

    IOcifNodeMutable setPosition(OcifVector23D position);

    IOcifNodeMutable setRelation(String relation);

    IOcifNodeMutable setResource(String resource);

    IOcifNodeMutable setResourceFit(ResourceFit resourceFit);

    IOcifNodeMutable setRotation(Double rotation);

    IOcifNodeMutable setSize(OcifVector23D size);

}
