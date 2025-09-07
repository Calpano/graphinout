package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioPort;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.foundation.json.JsonException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;

public class Gio2CjWriter implements GioWriter {

    private final CjWriter cjWriter;
    private boolean edgeDefaultDirected;

    public Gio2CjWriter(CjWriter cjWriter) {this.cjWriter = cjWriter;}

    @Override
    public void arrayEnd() throws JsonException {

    }

    @Override
    public void arrayStart() throws JsonException {

    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        cjWriter.baseUri(baseUri);
    }

    @Override
    public void data(GioData data) throws IOException {

    }

    @Override
    public void endDocument() throws IOException {
        cjWriter.documentEnd();
    }

    @Override
    public void endEdge() throws IOException {
        cjWriter.edgeEnd();
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        cjWriter.graphEnd();
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        cjWriter.nodeEnd();
    }

    @Override
    public void endPort() throws IOException {
        cjWriter.portEnd();
    }

    @Override
    public void key(GioKey gioKey) throws IOException {

    }

    @Override
    public void objectEnd() throws JsonException {

    }

    @Override
    public void objectStart() throws JsonException {

    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {

    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {

    }

    @Override
    public void onBoolean(boolean b) throws JsonException {

    }

    @Override
    public void onDouble(double d) throws JsonException {

    }

    @Override
    public void onFloat(float f) throws JsonException {

    }

    @Override
    public void onInteger(int i) throws JsonException {

    }

    @Override
    public void onKey(String key) throws JsonException {

    }

    @Override
    public void onLong(long l) throws JsonException {

    }

    @Override
    public void onNull() throws JsonException {

    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        cjWriter.documentStart();
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        cjWriter.edgeStart();
        for (GioEndpoint ep : edge.getEndpoints()) {
            cjWriter.endpointStart();
            if (ep.getId() != null) cjWriter.id(ep.getId());
            if (ep.getNode() != null) cjWriter.nodeId(ep.getNode());
            if (ep.getPort() != null) cjWriter.portId(ep.getPort());
            if (ep.getType() != null) cjWriter.direction(ep.getType().toCjDirection());
            cjWriter.endpointEnd();
        }
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        cjWriter.graphStart();
        if (gioGraph.getId() != null) cjWriter.id(gioGraph.getId());
        this.edgeDefaultDirected = gioGraph.isEdgedefaultDirected();
    }

    @Override
    public void startNode(GioNode node) throws IOException {

    }

    @Override
    public void startPort(GioPort port) throws IOException {

    }

    @Override
    public void onString(String s) throws JsonException {

    }

}
