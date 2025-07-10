package com.calpano.graphinout.base.cj.impl;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjEdgeType;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.impl.LoggingJsonWriter;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.slf4j.LoggerFactory.getLogger;

public class LoggingCjWriter extends LoggingJsonWriter implements CjWriter {

    public enum JsonEvent {
        ArrayStart, ArrayEnd, DocumentStart, DocumentEnd, ObjectStart, ObjectEnd, OnNumber, OnBoolean, OnKey, OnNull, OnString
    }

    public enum CjEvent {
        EdgeEnd, EdgeStart, EndpointEnd, EndpointStart, GraphEnd, GraphStart, JsonEnd, JsonStart, NodeEnd, NodeStart, PortEnd, PortStart, Id, BaseUri, EdgeDefault, Directed, EdgeType, NodeId, PortId, Direction, DocumentStart, DocumentEnd, LabelStart, LabelEnd, Language, LabelEntryStart, LabelEntryEnd, Value
    }
    private static final Logger log = getLogger(LoggingCjWriter.class);
    private final StringBuilder bufJson = new StringBuilder();
    private final StringBuilder bufCj = new StringBuilder();

    @Override
    public void arrayEnd() throws JsonException {
        onJson(JsonEvent.ArrayEnd, null);
    }

    @Override
    public void arrayStart() throws JsonException {
        onJson(JsonEvent.ArrayStart, null);
    }

    @Override
    public void baseUri(String baseuri) {
        onCj(CjEvent.BaseUri, baseuri);
    }

    @Override
    public void direction(CjDirection direction) {
        onCj(CjEvent.Direction, direction);
    }

    @Override
    public void documentEnd() throws JsonException {
        onCj(CjEvent.DocumentEnd, null);
        log.debug("CJ: " + bufCj);
        log.debug("JSON: " + bufJson);
    }

    @Override
    public void documentStart() throws JsonException {
        onCj(CjEvent.DocumentStart, null);
    }

    public void dump() {
        log.info("JSON: " + bufJson);
        log.info("CJ: " + bufCj);
    }

    @Override
    public void edgeEnd() {
        onCj(CjEvent.EdgeEnd, null);
    }

    @Override
    public void edgeStart() {
        onCj(CjEvent.EdgeStart, null);
    }

    @Override
    public void edgeType(CjEdgeType edgeType) {
        onCj(CjEvent.EdgeType, edgeType);
    }

    @Override
    public void edgeDefault(String edgedefault) {
        onCj(CjEvent.EdgeDefault, edgedefault);
    }

    @Override
    public void endpointEnd() {
        onCj(CjEvent.EndpointEnd, null);
    }

    @Override
    public void endpointStart() {
        onCj(CjEvent.EndpointStart, null);
    }

    @Override
    public void graphEnd() throws CjException {
        onCj(CjEvent.GraphEnd, null);
    }

    @Override
    public void graphStart() throws CjException {
        onCj(CjEvent.GraphStart, null);

    }

    @Override
    public void id(String id) {
        onCj(CjEvent.Id, id);
    }

    @Override
    public void isDirected(boolean isDirected) {
        onCj(CjEvent.Directed, isDirected);
    }

    @Override
    public void jsonEnd() {
        onCj(CjEvent.JsonEnd, bufJson.toString());
        bufJson.setLength(0);
    }

    @Override
    public void jsonStart() {
        onCj(CjEvent.JsonStart, null);
    }

    @Override
    public void labelEnd() {
        onCj(CjEvent.LabelEnd, null);
    }

    @Override
    public void language(String lang) {
        onCj(CjEvent.Language, lang);
    }

    @Override
    public void labelStart() {
        onCj(CjEvent.LabelStart, null);
    }

    @Override
    public void labelEntryEnd() {
        onCj(CjEvent.LabelEntryStart, null);
    }

    @Override
    public void labelEntryStart() {
        onCj(CjEvent.LabelEntryEnd, null);
    }

    @Override
    public void value(String value) {
        onCj(CjEvent.Value, value);
    }

    /** no newlines */
    public void logJson(String s) {
        bufJson.append(s);
    }

    @Override
    public void nodeEnd() {
        onCj(CjEvent.NodeEnd, null);
    }

    @Override
    public void nodeId(String nodeId) {
        onCj(CjEvent.NodeId, nodeId);
    }

    @Override
    public void nodeStart() {
        onCj(CjEvent.NodeStart, null);
    }

    @Override
    public void objectEnd() throws JsonException {
        onJson(JsonEvent.ObjectEnd, null);
    }

    @Override
    public void objectStart() throws JsonException {
        onJson(JsonEvent.ObjectStart, null);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        onJson(JsonEvent.OnNumber, "" + bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        onJson(JsonEvent.OnNumber, "" + bigInteger);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        onJson(JsonEvent.OnBoolean, "" + b);
    }

    public void onCj(CjEvent event, @Nullable Object o) {
        if (event.name().endsWith("Start")) {
            bufCj.append("<").append(event.name(), 0, event.name().length() - 5).append(">");
        } else if (event.name().endsWith("End")) {
            bufCj.append("</").append(event.name(), 0, event.name().length() - 3).append(">");
        } else {
            if (o == null) {
                bufCj.append("<").append(event.name()).append("/>");
            } else {
                bufCj.append("<").append(event.name()).append(" '");
                bufCj.append(o);
                bufCj.append("'/>");
            }
        }
    }

    @Override
    public void onDouble(double d) throws JsonException {
        onJson(JsonEvent.OnNumber, "" + d);

    }

    @Override
    public void onFloat(float f) throws JsonException {
        onJson(JsonEvent.OnNumber, "" + f);

    }

    @Override
    public void onInteger(int i) throws JsonException {
        onJson(JsonEvent.OnNumber, "" + i);
    }

    public void onJson(JsonEvent event, @Nullable Object o) {
        switch (event) {
            case OnKey -> logJson("【" + o + "】");
            case OnString -> logJson(" '" + o + "'");
            case OnNumber -> logJson(" number(" + o + ")");
            case OnBoolean -> logJson(" boolean(" + o + ")");
            case OnNull -> logJson(" null()");
            case ArrayStart -> logJson("[");
            case ArrayEnd -> logJson("]");
            case ObjectStart -> logJson("\n{");
            case ObjectEnd -> logJson("}");
            case DocumentStart -> logJson("documentStart()");
            case DocumentEnd -> logJson("documentEnd()");
        }
    }

    @Override
    public void onKey(String key) throws JsonException {
        onJson(JsonEvent.OnKey, key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        onJson(JsonEvent.OnNumber, "" + l);
    }

    @Override
    public void onNull() throws JsonException {
        onJson(JsonEvent.OnNull, null);
    }

    @Override
    public void portEnd() {
        onCj(CjEvent.PortEnd, null);
    }

    @Override
    public void portId(String portId) {
        onCj(CjEvent.PortId, portId);
    }

    @Override
    public void portStart() {
        onCj(CjEvent.PortStart, null);
    }

}
