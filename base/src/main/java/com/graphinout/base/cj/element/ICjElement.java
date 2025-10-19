package com.graphinout.base.cj.element;

import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.stream.ICjWriter;

import java.util.stream.Stream;

/**
 * Base interface for all CJ model elements (document, graph, node, edge, port, labels, etc.).
 * It defines common operations and traversal across the CJ tree while streaming or transforming graphs.
 */
public interface ICjElement {

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

    /** Fires this element, including start and end and its children. */
    void fire(ICjWriter cjWriter);

    Stream<ICjElement> directChildren();

    /** including this */
    default Stream<ICjElement> allElements() {
        return Stream.concat(Stream.of(this), directChildren().flatMap(ICjElement::allElements));
    }

}
