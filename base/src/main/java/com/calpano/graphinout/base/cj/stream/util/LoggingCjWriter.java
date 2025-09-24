package com.calpano.graphinout.base.cj.stream.util;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.CjException;
import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.stream.impl.LoggingJsonWriter;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Stack;

import static com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter.JsonEvent.ArrayStart;
import static com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter.JsonEvent.ObjectStart;
import static com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter.JsonEvent.OnBoolean;
import static com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter.JsonEvent.OnKey;
import static com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter.JsonEvent.OnNull;
import static com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter.JsonEvent.OnNumber;
import static com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter.JsonEvent.OnString;
import static org.slf4j.LoggerFactory.getLogger;

public class LoggingCjWriter extends LoggingJsonWriter implements ICjWriter {

    public enum JsonEvent {
        ArrayStart, ArrayEnd, DocumentStart, DocumentEnd, ObjectStart, ObjectEnd, OnNumber, OnBoolean, OnKey, OnNull, StringStart, StringEnd, String, OnString
    }

    public enum CjEvent {
        ArrayOfGraphsStart, ArrayOfGraphsEnd, ArrayOfEdgesStart, ArrayOfEdgesEnd, ArrayOfNodesStart, ArrayOfNodesEnd, ArrayOfPortsStart, ArrayOfPortsEnd, ArrayOfLabelEntriesStart, ArrayOfLabelEntriesEnd, ArrayOfEndpointsStart, ArrayOfEndpointsEnd, ArrayOfLabelEntryStart, ArrayOfLabelEntryEnd,

        ConnectedJsonStart, ConnectedJsonEnd, ConnectedJson__Canonical, ConnectedJson__VersionDate, ConnectedJson__VersionNumber,

        EdgeEnd, EdgeStart, EndpointEnd, EndpointStart, GraphEnd, GraphStart, NodeEnd, NodeStart, PortEnd, PortStart, Id, BaseUri, EdgeDefault, EdgeType, NodeId, PortId, Direction, DocumentStart, DocumentEnd, LabelStart, LabelEnd, Language, LabelEntryStart, LabelEntryEnd, Value, DataStart, DataEnd
    }

    enum CommaState {First, Container, Key}

    private static final Logger log = getLogger(LoggingCjWriter.class);
    private final StringBuilder bufCj = new StringBuilder();
    private final Stack<CommaState> commaStack = new Stack<>();
    private final boolean buffering;

    public LoggingCjWriter() {this(true);}

    public LoggingCjWriter(boolean buffering) {this.buffering = buffering;}

    @Override
    public void arrayEnd() throws JsonException {
        onJson(JsonEvent.ArrayEnd, null);
    }

    @Override
    public void arrayStart() throws JsonException {
        onJson(ArrayStart, null);
    }

    @Override
    public void baseUri(String baseUri) {
        onCj(CjEvent.BaseUri, baseUri);
    }

    @Override
    public void connectedJsonEnd() {
        onCj(CjEvent.ConnectedJsonEnd, null);
    }

    @Override
    public void connectedJsonStart() {
        onCj(CjEvent.ConnectedJsonStart, null);
    }

    public void connectedJson__canonical(boolean b) {
        onCj(CjEvent.ConnectedJson__Canonical, b);
    }

    public void connectedJson__versionDate(String versionDate) {
        onCj(CjEvent.ConnectedJson__VersionDate, versionDate);
    }

    public void connectedJson__versionNumber(String versionNumber) {
        onCj(CjEvent.ConnectedJson__VersionNumber, versionNumber);
    }

    @Override
    public void direction(CjDirection direction) {
        onCj(CjEvent.Direction, direction);
    }

    @Override
    public void documentEnd() throws JsonException {
        onCj(CjEvent.DocumentEnd, null);
        log.debug("CJ: {}", bufCj);
    }

    @Override
    public void documentStart() throws JsonException {
        onCj(CjEvent.DocumentStart, null);
    }

    public void dump() {
        log.info("CJ: {}", bufCj);
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
    public void edgeType(ICjEdgeType edgeType) {
        onCj(CjEvent.EdgeType, edgeType);
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
    public void jsonDataEnd() {
        onCj(CjEvent.DataEnd, null);
    }

    @Override
    public void jsonDataStart() {
        onCj(CjEvent.DataStart, null);
    }

    @Override
    public void labelEnd() {
        onCj(CjEvent.LabelEnd, null);
    }

    @Override
    public void labelEntryEnd() {
        onCj(CjEvent.LabelEntryEnd, null);
    }

    @Override
    public void labelEntryStart() {
        onCj(CjEvent.LabelEntryStart, null);
    }

    @Override
    public void labelStart() {
        onCj(CjEvent.LabelStart, null);
    }

    @Override
    public void language(String lang) {
        onCj(CjEvent.Language, lang);
    }

    @Override
    public void listEnd(CjType cjType) {
        onCj(CjEvent.valueOf(cjType.name() + "End"), null);

    }

    @Override
    public void listStart(CjType cjType) {
        onCj(CjEvent.valueOf(cjType.name() + "Start"), null);
    }

    /** no newlines */
    public void logJson(String s) {
        buffer(s);
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
        onJson(ObjectStart, null);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        onJson(OnNumber, "" + bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        onJson(OnNumber, "" + bigInteger);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        onJson(OnBoolean, "" + b);
    }

    public void onCj(CjEvent event, @Nullable Object o) {
        if (event.name().endsWith("Start")) {
            buffer("<");
            buffer(event.name().substring(0, event.name().length() - 5));
            buffer(">");
        } else if (event.name().endsWith("End")) {
            buffer("</");
            buffer(event.name().substring(0, event.name().length() - 3));
            buffer(">");
        } else {
            if (o == null) {
                buffer("<");
                buffer(event.name());
                buffer("/>");
            } else {
                buffer("<");
                buffer(event.name());
                buffer(" '");
                buffer(o.toString());
                buffer("' />");
            }
        }
    }

    @Override
    public void onDouble(double d) throws JsonException {
        onJson(OnNumber, "" + d);

    }

    @Override
    public void onFloat(float f) throws JsonException {
        onJson(OnNumber, "" + f);

    }

    @Override
    public void onInteger(int i) throws JsonException {
        onJson(OnNumber, "" + i);
    }

    public void onJson(JsonEvent event, @Nullable Object o) {
        // make comma
        if (EnumSet.of(ArrayStart, ObjectStart, OnNumber, OnString, JsonEvent.String, OnBoolean, OnNull, OnKey).contains(event)) {
            if (!commaStack.isEmpty()) {
                switch (commaStack.peek()) {
                    // First marker blocks first comma
                    case First -> commaStack.pop();
                    case Key -> commaStack.pop();
                    default -> logJson(", ");
                }
            }
        }
        // manage comma stack
        switch (event) {
            case ArrayStart, ObjectStart -> {
                commaStack.push(CommaState.Container);
                commaStack.push(CommaState.First);
            }
            case ArrayEnd, ObjectEnd -> {
                assert !commaStack.isEmpty() : "commaStack should not be empty";
                // remove unused First marker
                if (commaStack.peek() == CommaState.First) {
                    commaStack.pop();
                }
                commaStack.pop();
            }
            case OnKey -> commaStack.push(CommaState.Key);
            default -> {
            }
        }

        // content
        switch (event) {
            case OnKey -> logJson("【" + o + "】: ");
            case OnString, String -> logJson(" '" + o + "'");
            case OnNumber -> logJson(" number(" + o + ")");
            case OnBoolean -> logJson(" boolean(" + o + ")");
            case OnNull -> logJson(" null()");
            case ArrayStart -> logJson("[");
            case ArrayEnd -> logJson("]");
            case ObjectStart -> logJson("◀{ ");
            case ObjectEnd -> logJson(" }▶");
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
        onJson(OnNumber, "" + l);
    }

    @Override
    public void onNull() throws JsonException {
        onJson(OnNull, null);
    }

    @Override
    public void onString(String s) throws JsonException {
        onJson(JsonEvent.String, s);
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

    @Override
    public void value(String value) {
        onCj(CjEvent.Value, value);
    }

    private void buffer(String s) {
        if (buffering) {
            bufCj.append(s);
        } else {
            System.out.print(s);
        }
    }

}
