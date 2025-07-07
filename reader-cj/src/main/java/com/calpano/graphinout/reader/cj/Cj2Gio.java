package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.CjEventStream;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjLabel;
import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDataType;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

public class Cj2Gio implements CjEventStream {

    static class CjNode {

        public String id;
        public CjLabel label;

    }

    final GioWriter gio;
    private final StringBuilderJsonWriter jsonSink = new StringBuilderJsonWriter();
    private final Stack<Object> stack = new Stack<>();

    public Cj2Gio(GioWriter gio) {this.gio = gio;}

    @Override
    public void arrayEnd() throws JsonException {
        jsonSink.arrayEnd();
    }

    @Override
    public void arrayStart() throws JsonException {
        jsonSink.arrayStart();
    }

    @Override
    public void baseuri(String baseuri) {
        try {
            gio.baseUri(baseuri);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    @Override
    public void direction(CjDirection direction) {

    }

    @Override
    public void documentEnd() throws JsonException {
        try {
            gio.endDocument();
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    @Override
    public void documentStart() {
        try {
            gio.startDocument(null);
            gio.key(GioKey.builder().forType(GioKeyForType.All) //
                    .attributeName("json")//
                    .attributeType(GioDataType.typeString)//
                    .description("Connected JSON allows custom JSON properties in all objects")//
                    .build());
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    @Override
    public void edgeEnd() {
        try {
            gio.endEdge();
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public void edgeStart() {
        // TODO start buffering content for edge
    }

    @Override
    public void edgeType(CjEdgeType edgeType) {

    }

    @Override
    public void edgedefault(String edgedefault) {

    }

    @Override
    public void endpointEnd() {
        // Endpoints are leaf elements in CJ, no corresponding GIO operation needed
    }

    @Override
    public void endpointStart() {

        // Endpoints are leaf elements in CJ, no corresponding GIO operation needed
        // Endpoint information (node, port, direction, type) is handled at the edge level
    }

    @Override
    public void graphEnd() throws CjException {
        try {
            gio.endGraph(null);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    @Override
    public void graphStart() throws CjException {
        // TODO start buffering
    }

    @Override
    public void id(String id) {
        ((CjNode) stack.peek()).id = id;
    }

    @Override
    public void isDirected(boolean isDirected) {

    }

    @Override
    public void jsonEnd() {
        try {
            String json = jsonSink.json();
            gio.data(GioData.builder().key("json").value(json).build());
        } catch (IOException e) {
            throw new JsonException(e);
        }
        jsonSink.reset();
    }

    @Override
    public void jsonStart() {
        // jsonSink is ready to collect JSON data
        // No specific initialization needed as jsonSink is reset in jsonEnd()
    }

    @Override
    public void labelEnd() {

    }

    @Override
    public void labelEntryEnd() {

    }

    @Override
    public void labelEntryStart() {

    }

    @Override
    public void labelStart() {

    }

    @Override
    public void language(String lang) {

    }

    @Override
    public void nodeEnd() {
        CjNode node = (CjNode) stack.pop();
        GioNode gioNode = GioNode.builder().id(node.id).build();
        try {
            gio.startNode(gioNode);
            gio.endNode(null);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    @Override
    public void nodeId(String nodeId) {

    }

    @Override
    public void nodeStart() {
        stack.push(new CjNode());
    }

    @Override
    public void objectEnd() throws JsonException {
        jsonSink.objectEnd();
    }

    @Override
    public void objectStart() throws JsonException {
        jsonSink.objectStart();
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        jsonSink.onBigDecimal(bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) {
        jsonSink.onBigInteger(bigInteger);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        jsonSink.onBoolean(b);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        jsonSink.onDouble(d);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        jsonSink.onFloat(f);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        jsonSink.onInteger(i);
    }

    @Override
    public void onKey(String key) throws JsonException {
        jsonSink.onKey(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        jsonSink.onLong(l);
    }

    @Override
    public void onNull() throws JsonException {
        jsonSink.onNull();
    }

    @Override
    public void onString(String s) throws JsonException {
        jsonSink.onString(s);
    }

    @Override
    public void portEnd() {
        try {
            gio.endPort();
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    @Override
    public void portId(String portId) {

    }

    @Override
    public void portStart() {
        // TODO start buffering
    }

    @Override
    public void value(String value) {

    }

}
