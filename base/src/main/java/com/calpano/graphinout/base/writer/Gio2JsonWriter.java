package com.calpano.graphinout.base.writer;

import com.calpano.graphinout.base.cj.CjConstants;
import com.calpano.graphinout.base.cj.CjType;
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
import com.calpano.graphinout.foundation.json.JsonWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Objects;
import java.util.Stack;

// TODO use for writing CJ
@Deprecated
public class Gio2JsonWriter implements GioWriter {

    enum Mode {Json, Cj}

    /** the version of CJ that this writer writes */
    private static final String CJ_VERSION_NUMBER = "5.0.0";
    private static final String CJ_VERSION_DATA = "2025-07-14";
    final JsonWriter jsonWriter;
    private final Stack<CjType> stack = new Stack<>();
    private Mode mode = Mode.Cj;

    public Gio2JsonWriter(JsonWriter jsonWriter) {this.jsonWriter = jsonWriter;}

    @Override
    public void arrayEnd() throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.arrayEnd();
        } else {
            throw new IllegalStateException("Unexpected varrayEnd in CJ mode");
        }
    }

    @Override
    public void arrayStart() throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.arrayStart();
        } else {
            throw new IllegalStateException("Unexpected varrayStart in CJ mode");
        }
    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        jsonWriter.onKey(CjConstants.BASE_URI);
        jsonWriter.string(baseUri);
    }

    @Override
    public void data(GioData data) throws IOException {

    }

    @Override
    public void endDocument() throws IOException {
        jsonWriter.objectEnd();
        jsonWriter.documentEnd();
    }

    @Override
    public void endEdge() throws IOException {
        jsonWriter.objectEnd();
        pop(CjType.Edge);
    }

    @Override
    public void endGraph(@Nullable URL locator) throws IOException {
        jsonWriter.objectEnd();
        pop(CjType.Graph);
    }

    public void endJsonData() {
        assert mode == Mode.Json;
        mode = Mode.Cj;
    }

    @Override
    public void endNode(@Nullable URL locator) throws IOException {
        jsonWriter.objectEnd();
        pop(CjType.Node);
    }

    @Override
    public void endPort() throws IOException {
        jsonWriter.objectEnd();
        pop(CjType.Port);
    }

    /** start a JSON-value like raw data object */
    public void endRawData() {}

    @Override
    public void key(GioKey gioKey) throws IOException {

    }

    public void listEnd(CjType cjType) {
        CjType type = stack.pop();
        assert type == cjType;
    }

    public void listStart(CjType cjType) {
        stack.push(cjType);
    }

    @Override
    public void objectEnd() throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.objectEnd();
        } else {
            throw new IllegalStateException("Unexpected objectEnd in CJ mode");
        }
    }

    @Override
    public void objectStart() throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.objectStart();
        } else {
            throw new IllegalStateException("Unexpected objectStart in CJ mode");
        }
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onBigDecimal(bigDecimal);
        } else {
            throw new IllegalStateException("Unexpected onBigDecimal in CJ mode");
        }
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onBigInteger(bigInteger);
        } else {
            throw new IllegalStateException("Unexpected onBigInteger in CJ mode");
        }
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onBoolean(b);
        } else {
            throw new IllegalStateException("Unexpected onBoolean in CJ mode");
        }
    }

    @Override
    public void onDouble(double d) throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onDouble(d);
        } else {
            throw new IllegalStateException("Unexpected onDouble in CJ mode");
        }
    }

    @Override
    public void onFloat(float f) throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onFloat(f);
        } else {
            throw new IllegalStateException("Unexpected onFloat in CJ mode");
        }
    }

    @Override
    public void onInteger(int i) throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onInteger(i);
        } else {
            throw new IllegalStateException("Unexpected onInteger in CJ mode");
        }
    }

    @Override
    public void onKey(String key) throws JsonException {

    }

    @Override
    public void onLong(long l) throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onLong(l);
        } else {
            throw new IllegalStateException("Unexpected onLong in CJ mode");
        }
    }

    @Override
    public void onNull() throws JsonException {
        if (Objects.requireNonNull(mode) == Mode.Json) {
            jsonWriter.onNull();
        } else {
            throw new IllegalStateException("Unexpected onNull in CJ mode");
        }
    }

    /** can be called multiple times per data string */
    public void rawDataCharacters(String data) {}

    @Override
    public void startDocument(GioDocument document) throws IOException {
        jsonWriter.documentStart();
        jsonWriter.objectStart();

        jsonWriter.onKey(CjConstants.CONNECTED_JSON);
        jsonWriter.objectStart();
        jsonWriter.onKey(CjConstants.VERSION_NUMBER);
        jsonWriter.string(CJ_VERSION_NUMBER);
        jsonWriter.onKey(CjConstants.VERSION_DATE);
        jsonWriter.string(CJ_VERSION_DATA);
        jsonWriter.objectEnd();
    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {
        ensureIArrayOf(CjType.ArrayOfEdges);
        stack.push(CjType.Edge);

        jsonWriter.objectStart();
        String id = edge.getId();
        if (id != null && !id.isEmpty()) {
            jsonWriter.onKey(CjConstants.ID);
            jsonWriter.string(id);
        }

        jsonWriter.onKey(CjConstants.ENDPOINTS);
        jsonWriter.arrayStart();
        for (GioEndpoint ep : edge.getEndpoints()) {
            jsonWriter.objectStart();
            jsonWriter.onKey(CjConstants.NODE);
            jsonWriter.string(ep.getNode());
            String portId = ep.getPort();
            if (portId != null && !portId.isEmpty()) {
                jsonWriter.onKey(CjConstants.PORT);
                jsonWriter.string(portId);
            }
            jsonWriter.onKey(CjConstants.DIRECTION);
            jsonWriter.string(         ep.getType().xmlValue );
            // TODO type
        }
        jsonWriter.arrayEnd();
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        ensureIArrayOf(CjType.ArrayOfGraphs);
        stack.push(CjType.Graph);

        jsonWriter.objectStart();
        jsonWriter.onKey(CjConstants.ID);
        jsonWriter.string(gioGraph.getId());

        // TODO compoundNode from gioGraph (not yet there)
        // TODO label from gioGraph (not yet there)
        // TODO meta ??? from gioGraph (not yet there)

    }

    public void startJsonData() {
        assert mode == Mode.Cj;
        mode = Mode.Json;
    }

    @Override
    public void startNode(GioNode node) throws IOException {
        ensureIArrayOf(CjType.ArrayOfNodes);
        stack.push(CjType.Node);

        jsonWriter.objectStart();
        jsonWriter.onKey(CjConstants.ID);
        jsonWriter.string(node.getId());
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        ensureIArrayOf(CjType.ArrayOfPorts);
        stack.push(CjType.Port);

        jsonWriter.objectStart();
        jsonWriter.onKey(CjConstants.ID);
        jsonWriter.string(port.getName());
    }

    /** end a JSON-value like raw data object */
    public void startRawData() {}

    @Override
    public void stringCharacters(String s) throws JsonException {

    }

    @Override
    public void stringEnd() throws JsonException {

    }

    @Override
    public void stringStart() throws JsonException {

    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        // TODO let it through?
        jsonWriter.whitespaceCharacters(s);
    }

    private void ensureIArrayOf(CjType desiredArray) {
        assert desiredArray.isArray();
        CjType cjType = stack.peek();
        if (cjType.isArray()) {
            if (cjType == desiredArray) {
                return;
            }
            // close the open, wrong array
            jsonWriter.arrayEnd();
        }
        switch (desiredArray) {
            case ArrayOfEdges -> jsonWriter.onKey(CjConstants.EDGES);
            case ArrayOfNodes -> jsonWriter.onKey(CjConstants.NODES);
            case ArrayOfGraphs -> jsonWriter.onKey(CjConstants.GRAPHS);
            case ArrayOfPorts -> jsonWriter.onKey(CjConstants.PORTS);
            case ArrayOfEndpoints -> jsonWriter.onKey(CjConstants.ENDPOINTS);
            default -> throw new IllegalStateException("Unknown desired array " + desiredArray);
        }
        jsonWriter.arrayStart();
    }

    private void pop(CjType cjType) {
        CjType x = stack.pop();
        assert x == cjType;
    }

}
