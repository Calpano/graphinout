package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;

public interface IOcifNodeMutable extends IOcifNode {

    IOcifNodeMutable addExtension(IOcifExtension ext);

    IOcifNodeMutable setData(IJsonArray data);

    IOcifNodeMutable setExtras(IJsonObject extras);

    IOcifNodeMutable setId(String id);

    IOcifNodeMutable setPosition(double[] position);

    IOcifNodeMutable setRelation(String relation);

    IOcifNodeMutable setResource(String resource);

    IOcifNodeMutable setResourceFit(ResourceFit resourceFit);

    IOcifNodeMutable setRotation(Double rotation);

    IOcifNodeMutable setSize(double[] size);

}
