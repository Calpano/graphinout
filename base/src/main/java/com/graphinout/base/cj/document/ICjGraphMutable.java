package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.value.ObjectRef;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrGetDefault;

/**
 * Mutable variant of {@link ICjGraph} used while constructing a CJ document.
 */
public interface ICjGraphMutable extends ICjGraph, ICjHasGraphsMutable, ICjGraphChunkMutable, ICjHasLabelMutable {

    /**
     * Convenience method
     *
     * @param sourceNodeId incoming
     * @param targetNodeId outgoing
     * @return the created edge as a mutable variant for further modification
     */
    default ICjEdgeMutable addBiEdge(String sourceNodeId, String targetNodeId) {
        ObjectRef<ICjEdgeMutable> edgeMutableRef = ObjectRef.createNull();
        addEdge(edge -> {
            edgeMutableRef.set(edge);
            edge.addEndpointIncoming(sourceNodeId);
            edge.addEndpointOutgoing(targetNodeId);
        });
        return edgeMutableRef.get();
    }

    ICjEdgeMutable addEdge(Consumer<ICjEdgeMutable> edge);

    default ICjEdgeMutable addEdge() {
        return addEdge(edge -> {});
    }

    ICjNodeMutable addNode(Consumer<ICjNodeMutable> node);

    default ICjNodeMutable addNode() {
        return addNode(node -> {});
    }

    default ICjNodeMutable getOrCreateNodeWithId(String nodeId) {
        return nonNullOrGetDefault(findNodeById(nodeId), ICjElement::asNode, //
                () -> addNode(node -> node.id(nodeId)));
    }

    default void removeCoreElement(@NonNull ICjCoreElement coreElement) {
        switch (coreElement) {
            case ICjGraph graph -> removeGraph(graph);
            case ICjNode node -> removeNode(node);
            case ICjEdge edge -> removeEdge(edge);
            default ->
                    throw new IllegalArgumentException("Unsupported core element type: " + coreElement.getClass().getSimpleName());
        }
    }

    void removeNode(ICjNode node);

    void removeEdge(ICjEdge edge);

}

