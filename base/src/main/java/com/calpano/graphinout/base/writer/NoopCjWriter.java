package com.calpano.graphinout.base.writer;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

public class NoopCjWriter extends NoopJsonWriter implements ICjWriter {

    @Override
    public void baseUri(String baseUri) {}

    @Override
    public void connectedJsonEnd() {}

    @Override
    public void connectedJsonStart() {}

    @Override
    public void connectedJson__canonical(boolean b) {}

    @Override
    public void connectedJson__versionDate(String s) {}

    @Override
    public void connectedJson__versionNumber(String s) {}

    @Override
    public void direction(CjDirection direction) {}

    @Override
    public void edgeEnd() {}

    @Override
    public void edgeStart() {}

    @Override
    public void edgeType(ICjEdgeType edgeType) {}

    @Override
    public void endpointEnd() {}

    @Override
    public void endpointStart() {}

    @Override
    public void graphEnd() throws CjException {}

    @Override
    public void graphStart() throws CjException {}

    @Override
    public void id(String id) {}

    @Override
    public void jsonDataEnd() {}

    @Override
    public void jsonDataStart() {}

    @Override
    public void labelEnd() {}

    @Override
    public void labelEntryEnd() {}

    @Override
    public void labelEntryStart() {}

    @Override
    public void labelStart() {}

    @Override
    public void language(String language) {}

    @Override
    public void listEnd(CjType cjType) {}

    @Override
    public void listStart(CjType cjType) {}

    @Override
    public void nodeEnd() {}

    @Override
    public void nodeId(String nodeId) {}

    @Override
    public void nodeStart() {}

    @Override
    public void portEnd() {}

    @Override
    public void portId(String portId) {}

    @Override
    public void portStart() {}

    @Override
    public void value(String value) {}

}
