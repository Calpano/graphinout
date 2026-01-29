package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.collections.jajson.Json2JsonValueWriter;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;

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

    /** @throws ClassCastException if this is not a Document */
    default ICjDocumentMutable asDocument() {
        return (ICjDocumentMutable) this;
    }

    /** @throws ClassCastException if this is not a Edge */
    default ICjEdgeMutable asEdge() {
        return (ICjEdgeMutable) this;
    }

    /** @throws ClassCastException if this is not a Endpoint */
    default ICjEndpointMutable asEndpoint() {
        return (ICjEndpointMutable) this;
    }

    /** @throws ClassCastException if this is not a Graph */
    default ICjGraphMutable asGraph() {
        return (ICjGraphMutable) this;
    }

    /** @throws ClassCastException if this is not a HasGraphs */
    default ICjHasGraphsMutable asHasGraphsMutable() {
        return (ICjHasGraphsMutable) this;
    }

    /** @throws ClassCastException if this is not a Node */
    default ICjNodeMutable asNode() {
        return (ICjNodeMutable) this;
    }

    /** @throws ClassCastException if this is not a Port */
    default ICjPortMutable asPort() {
        return (ICjPortMutable) this;
    }

    /** @throws ClassCastException if this is not a HasData */
    default ICjHasDataMutable asWithData() {
        return (ICjHasDataMutable) this;
    }

    CjType cjType();

    Stream<ICjElement> directChildren();

    /** Fires this element, including start and end and its children. */
    void fire(ICjWriter cjWriter, boolean sort);

    default IJsonObject toJsonValue() {
        Json2JsonValueWriter json2JsonValueWriter = new Json2JsonValueWriter(IJsonFactory.INSTANCE);
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2JsonValueWriter);
        cj2JsonWriter.objectStart();
        fire(cj2JsonWriter, true);
        cj2JsonWriter.objectEnd();
        return Objects.requireNonNull(json2JsonValueWriter.resultJsonRootObject()).asObject();
    }


}
