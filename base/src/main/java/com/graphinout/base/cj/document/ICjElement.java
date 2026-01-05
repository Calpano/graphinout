package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.collections.jajson.Json2JsonValueWriter;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Base interface for all CJ model elements (document, graph, node, edge, port, labels, etc.). It defines common
 * operations and traversal across the CJ tree while streaming or transforming graphs.
 */
public interface ICjElement {

    /** including this */
    default Stream<ICjElement> allElements() {
        return Stream.concat(Stream.of(this), directChildren().flatMap(ICjElement::allElements));
    }

    default ICjDocumentMutable asDocument() {
        return (ICjDocumentMutable) this;
    }

    default ICjEdgeMutable asEdge() {
        return (ICjEdgeMutable) this;
    }

    default ICjEndpointMutable asEndpoint() {
        return (ICjEndpointMutable) this;
    }

    default ICjGraphMutable asGraph() {
        return (ICjGraphMutable) this;
    }

    default ICjNodeMutable asNode() {
        return (ICjNodeMutable) this;
    }

    default ICjPortMutable asPort() {
        return (ICjPortMutable) this;
    }

    default ICjHasDataMutable asWithData() {
        return (ICjHasDataMutable) this;
    }

    CjType cjType();

    Stream<ICjElement> directChildren();

    /** Fires this element, including start and end and its children. */
    void fire(ICjWriter cjWriter);

    default IJsonArray toJsonValue() {
        Json2JsonValueWriter json2JsonValueWriter = new Json2JsonValueWriter(IJsonFactory.INSTANCE);
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2JsonValueWriter);
        cj2JsonWriter.objectStart();
        fire(cj2JsonWriter);
        cj2JsonWriter.objectEnd();
        return Objects.requireNonNull(json2JsonValueWriter.resultJsonRootObject()).asArray();
    }

}
