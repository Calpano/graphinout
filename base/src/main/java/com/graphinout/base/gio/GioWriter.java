package com.graphinout.base.gio;

import com.graphinout.base.reader.Locator;
import com.graphinout.foundation.json.stream.JsonValueWriter;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.xml.XmlWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

/**
 * This interface allows pushing parser events as GIO. Usually used with more basic writers like an {@link XmlWriter} or
 * {@link JsonWriter}.
 * <p>
 * Also, implementations of this interface convert GIO into syntaxes like CJ/JSON or GraphML/XML.
 * <p>
 * For large files, we don't want to keep the entire graph object in memory.
 */
public interface GioWriter extends JsonValueWriter {

    void baseUri(String baseUri) throws IOException;

    void data(GioData data) throws IOException;

    void endDocument() throws IOException;

    void endEdge() throws IOException;

    void endGraph(@Nullable URL locator) throws IOException;

    default void endJsonData() {}

    void endNode(@Nullable URL locator) throws IOException;

    void endPort() throws IOException;

    /** start a JSON-value like raw data object */
    default void endRawData() {}

    void key(GioKey gioKey) throws IOException;

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

}
