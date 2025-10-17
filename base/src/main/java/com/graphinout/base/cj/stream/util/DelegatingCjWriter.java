package com.graphinout.base.cj.stream.util;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.ICjEdgeType;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.json.stream.impl.DelegatingJsonWriter;

import java.util.function.Consumer;

public class DelegatingCjWriter extends DelegatingJsonWriter implements ICjWriter {

    public DelegatingCjWriter(ICjWriter cjWriter) {
        super(cjWriter);
    }

    public DelegatingCjWriter addCjWriter(ICjWriter cjWriter) {
        super.addJsonWriter(cjWriter);
        return this;
    }

    @Override
    public void baseUri(String baseUri) {
        forEachWriter(cjWriter -> cjWriter.baseUri(baseUri));
    }

    @Override
    public void connectedJsonEnd() {
        forEachWriter(ICjWriter::connectedJsonEnd);
    }

    @Override
    public void connectedJsonStart() {
        forEachWriter(ICjWriter::connectedJsonStart);
    }

    @Override
    public void connectedJson__canonical(boolean b) {
        forEachWriter(cjWriter -> cjWriter.connectedJson__canonical(b));
    }

    @Override
    public void connectedJson__versionDate(String s) {
        forEachWriter(cjWriter -> cjWriter.connectedJson__versionDate(s));
    }

    @Override
    public void connectedJson__versionNumber(String s) {
        forEachWriter(cjWriter -> cjWriter.connectedJson__versionNumber(s));
    }

    @Override
    public void direction(CjDirection direction) {
        forEachWriter(cjWriter -> cjWriter.direction(direction));
    }

    @Override
    public void documentEnd() {
        forEachWriter(ICjWriter::documentEnd);
    }

    @Override
    public void documentStart() {
        forEachWriter(ICjWriter::documentStart);
    }

    @Override
    public void edgeEnd() {
        forEachWriter(ICjWriter::edgeEnd);
    }

    @Override
    public void edgeStart() {
        forEachWriter(ICjWriter::edgeStart);
    }

    @Override
    public void edgeType(ICjEdgeType edgeType) {
        forEachWriter(cjWriter -> cjWriter.edgeType(edgeType));
    }

    @Override
    public void endpointEnd() {
        forEachWriter(ICjWriter::endpointEnd);
    }

    @Override
    public void endpointStart() {
        forEachWriter(ICjWriter::endpointStart);
    }

    @Override
    public void graphEnd() {
        forEachWriter(ICjWriter::graphEnd);
    }

    @Override
    public void graphStart() {
        forEachWriter(ICjWriter::graphStart);
    }

    @Override
    public void id(String id) {
        forEachWriter(cjWriter -> cjWriter.id(id));
    }

    @Override
    public void jsonDataEnd() {
        forEachWriter(ICjWriter::jsonDataEnd);
    }

    @Override
    public void jsonDataStart() {
        forEachWriter(ICjWriter::jsonDataStart);
    }

    @Override
    public void labelEnd() {
        forEachWriter(ICjWriter::labelEnd);
    }

    @Override
    public void labelEntryEnd() {
        forEachWriter(ICjWriter::labelEntryEnd);
    }

    @Override
    public void labelEntryStart() {
        forEachWriter(ICjWriter::labelEntryStart);
    }

    @Override
    public void labelStart() {
        forEachWriter(ICjWriter::labelStart);
    }

    @Override
    public void language(String language) {
        forEachWriter(cjWriter -> cjWriter.language(language));
    }

    @Override
    public void listEnd(CjType cjType) {
        forEachWriter(cjWriter -> cjWriter.listEnd(cjType));
    }

    @Override
    public void listStart(CjType cjType) {
        forEachWriter(cjWriter -> cjWriter.listStart(cjType));
    }

    @Override
    public void nodeEnd() {
        forEachWriter(ICjWriter::nodeEnd);
    }

    @Override
    public void nodeId(String nodeId) {
        forEachWriter(cjWriter -> cjWriter.nodeId(nodeId));
    }

    @Override
    public void nodeStart() {
        forEachWriter(ICjWriter::nodeStart);
    }

    @Override
    public void portEnd() {
        forEachWriter(ICjWriter::portEnd);
    }

    @Override
    public void portId(String portId) {
        forEachWriter(cjWriter -> cjWriter.portId(portId));
    }

    @Override
    public void portStart() {
        forEachWriter(ICjWriter::portStart);
    }

    @Override
    public void value(String value) {
        forEachWriter(cjWriter -> cjWriter.value(value));
    }

    private void forEachWriter(Consumer<ICjWriter> consumer) {
        super.jsonWriters.forEach(w -> consumer.accept((ICjWriter) w));
    }

}
