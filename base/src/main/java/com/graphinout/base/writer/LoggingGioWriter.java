package com.graphinout.base.writer;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioPort;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.foundation.json.JsonException;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Does not produce Graphml, rather, just logs
 */
public class LoggingGioWriter implements GioWriter {

    private static final Logger log = getLogger(LoggingGioWriter.class);

    @Override
    public void arrayEnd() {
        log.info("GioWriter: arrayEnd()");
    }

    @Override
    public void arrayStart() {
        log.info("GioWriter: arrayStart()");
    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        log.info("GioWriter: baseuri(" + baseUri + ")");
    }

    @Override
    public void data(GioData data) throws IOException {
        log.info("GioWriter: data(GioData " + data + ")");
    }

    @Override
    public void endDocument() throws IOException {
        log.info("GioWriter: endDocument()");
    }

    @Override
    public void endEdge() throws IOException {
        log.info("GioWriter: endEdge()");
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        log.info("GioWriter: endGraph(" + locator + ")");
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        log.info("GioWriter: endNode(" + locator + ")");
    }

    @Override
    public void endPort() throws IOException {
        log.info("GioWriter: endPort()");
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        log.info("GioWriter: key(GioKey " + gioKey + ")");
    }

    @Override
    public void objectEnd() {
        log.info("GioWriter: objectEnd()");
    }

    @Override
    public void objectStart() {
        log.info("GioWriter: objectStart()");
    }

    @Override
    public void onBigDecimal(java.math.BigDecimal bigDecimal) {
        log.info("GioWriter: onBigDecimal(" + bigDecimal + ")");
    }

    @Override
    public void onBigInteger(java.math.BigInteger bigInteger) {
        log.info("GioWriter: onBigInteger(" + bigInteger + ")");
    }

    @Override
    public void onBoolean(boolean b) {
        log.info("GioWriter: onBoolean(" + b + ")");
    }

    @Override
    public void onDouble(double d) {
        log.info("GioWriter: onDouble(" + d + ")");
    }

    @Override
    public void onFloat(float f) {
        log.info("GioWriter: onFloat(" + f + ")");
    }

    @Override
    public void onInteger(int i) {
        log.info("GioWriter: onInteger(" + i + ")");
    }

    @Override
    public void onKey(String key) {
        log.info("GioWriter: onKey(" + key + ")");
    }

    @Override
    public void onLong(long l) {
        log.info("GioWriter: onLong(" + l + ")");
    }

    @Override
    public void onNull() {
        log.info("GioWriter: onNull()");
    }

    @Override
    public void onString(String s) throws JsonException {
        log.info("GioWriter: string(" + s + ")");
    }


    @Override
    public void startDocument(GioDocument document) throws IOException {
        log.info("GioWriter: startDocument(GioDocument " + document + ")");
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        log.info("GioWriter: startEdge(GioEdge " + edge + ")");
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        log.info("GioWriter: startGraph(GioGraph " + gioGraph + ")");
    }

    @Override
    public void startNode(GioNode node) throws IOException {
        log.info("GioWriter: startNode(GioNode " + node + ")");
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        log.info("GioWriter: startPort(GioPort " + port + ")");
    }


}
