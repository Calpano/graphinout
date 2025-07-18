package com.calpano.graphinout.base.cj.impl;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.foundation.json.impl.DelegatingJsonWriter;

import java.util.function.Consumer;

public class DelegatingCjWriter extends DelegatingJsonWriter implements CjWriter {

    public DelegatingCjWriter(CjWriter cjWriter) {
        super(cjWriter);
    }

    public void addCjWriter(CjWriter cjWriter) {
        super.addWriter(cjWriter);
    }

    @Override
    public void baseUri(String baseUri) {
        forEachWriter(cjWriter -> cjWriter.baseUri(baseUri));
    }

    @Override
    public void direction(CjDirection direction) {
        forEachWriter(cjWriter -> cjWriter.direction(direction));
    }

    @Override
    public void documentEnd() {
        forEachWriter(CjWriter::documentEnd);
    }

    @Override
    public void documentStart() {
        forEachWriter(CjWriter::documentStart);
    }

    @Override
    public void edgeEnd() {
        forEachWriter(CjWriter::edgeEnd);
    }

    @Override
    public void edgeStart() {
        forEachWriter(CjWriter::edgeStart);
    }

    @Override
    public void edgeType(CjEdgeType edgeType) {
        forEachWriter(cjWriter -> cjWriter.edgeType(edgeType));
    }

    @Override
    public void endpointEnd() {
        forEachWriter(CjWriter::endpointEnd);
    }

    @Override
    public void endpointStart() {
        forEachWriter(CjWriter::endpointStart);
    }

    @Override
    public void graphEnd() {
        forEachWriter(CjWriter::graphEnd);
    }

    @Override
    public void graphStart() {
        forEachWriter(CjWriter::graphStart);
    }

    @Override
    public void id(String id) {
        forEachWriter(cjWriter -> cjWriter.id(id));
    }

    @Override
    public void jsonEnd() {
        forEachWriter(CjWriter::jsonEnd);
    }

    @Override
    public void jsonStart() {
        forEachWriter(CjWriter::jsonStart);
    }

    @Override
    public void labelEnd() {
        forEachWriter(CjWriter::labelEnd);
    }

    @Override
    public void labelEntryEnd() {
        forEachWriter(CjWriter::labelEntryEnd);
    }

    @Override
    public void labelEntryStart() {
        forEachWriter(CjWriter::labelEntryStart);
    }

    @Override
    public void labelStart() {
        forEachWriter(CjWriter::labelStart);
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
        forEachWriter(CjWriter::nodeEnd);
    }

    @Override
    public void nodeId(String nodeId) {
        forEachWriter(cjWriter -> cjWriter.nodeId(nodeId));
    }

    @Override
    public void nodeStart() {
        forEachWriter(CjWriter::nodeStart);
    }

    @Override
    public void portEnd() {
        forEachWriter(CjWriter::portEnd);
    }

    @Override
    public void portId(String portId) {
        forEachWriter(cjWriter -> cjWriter.portId(portId));
    }

    @Override
    public void portStart() {
        forEachWriter(CjWriter::portStart);
    }

    @Override
    public void value(String value) {
        forEachWriter(cjWriter -> cjWriter.value(value));
    }

    private void forEachWriter(Consumer<CjWriter> consumer) {
        super.jsonWriters.forEach(w -> consumer.accept((CjWriter) w));
    }

}
