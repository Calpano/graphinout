package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.reader.Locator;
import com.calpano.graphinout.foundation.json.JsonElementWriter;
import com.calpano.graphinout.foundation.json.JsonException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GioWriter extends JsonElementWriter {

    void baseUri(String baseUri) throws IOException;

    @Deprecated
    void data(GioData data) throws IOException;

    void endDocument() throws IOException;

    void endEdge() throws IOException;

    void endGraph(@Nullable URL locator) throws IOException;

    default void endJsonData() {}

    void endNode(@Nullable URL locator) throws IOException;

    void endPort() throws IOException;

    /** start a JSON-value like raw data object */
    default void endRawData() {}

    @Deprecated
    void key(GioKey gioKey) throws IOException;

    default void listEnd(CjType cjType) {}

    default void listStart(CjType cjType) {}

    @Deprecated
    default void onString(String s) throws JsonException {
        stringStart();
        stringCharacters(s);
        stringEnd();
    }

    /** can be called multiple times per data string */
    default void rawDataCharacters(String data) {}

    /** Receive a {@link Locator}, that can be used to retrieve line:col information about the current parse location () */
    default void setLocator(Locator locator) {}

    void startDocument(GioDocument document) throws IOException;

    void startEdge(GioEdge edge) throws IOException;

    void startGraph(GioGraph gioGraph) throws IOException;

    default void startJsonData() {}

    /**
     * May contain #startGraph -- DTD is a bit unclear here whether 1 or multiple graphs are allowed. 1 seems more
     * plausible.
     */
    void startNode(GioNode node) throws IOException;

    void startPort(GioPort port) throws IOException;

    /** end a JSON-value like raw data object */
    default void startRawData() {}


}
