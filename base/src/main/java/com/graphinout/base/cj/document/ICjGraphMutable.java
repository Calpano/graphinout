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

    /**
     * Creates a new edge and allows customization through the provided consumer.
     *
     * @param edge a consumer to customize the newly created edge.
     * @return the created edge.
     */
    ICjEdgeMutable addEdge(Consumer<ICjEdgeMutable> edge);

    /**
     * Creates and adds a new empty edge.
     *
     * @return the created edge.
     */
    default ICjEdgeMutable addEdge() {
        return addEdge(edge -> {});
    }

    /**
     * Creates a new node and allows customization through the provided consumer.
     *
     * @param node a consumer to customize the newly created node.
     * @return the created node.
     */
    ICjNodeMutable addNode(Consumer<ICjNodeMutable> node);

    /**
     * Creates and adds a new empty node.
     *
     * @return the created node.
     */
    default ICjNodeMutable addNode() {
        return addNode(node -> {});
    }

    /**
     * Gets an existing node by ID or creates a new one with the given ID.
     *
     * @param nodeId the ID of the node.
     * @return the existing or newly created node.
     */
    default ICjNodeMutable getOrCreateNodeWithId(String nodeId) {
        return nonNullOrGetDefault(findNodeById(nodeId), ICjElement::asNode, //
                () -> addNode(node -> node.id(nodeId)));
    }

    /**
     * Removes a core element (graph, node, or edge) from this graph.
     *
     * @param coreElement the element to remove.
     */
    default void removeCoreElement(@NonNull ICjCoreElement coreElement) {
        switch (coreElement) {
            case ICjGraph graph -> removeGraph(graph);
            case ICjNode node -> removeNode(node);
            case ICjEdge edge -> removeEdge(edge);
            default ->
                    throw new IllegalArgumentException("Unsupported core element type: " + coreElement.getClass().getSimpleName());
        }
    }

    /**
     * Removes the specified node from this graph.
     *
     * @param node the node to remove.
     */
    void removeNode(ICjNode node);

    /**
     * Removes the specified edge from this graph.
     *
     * @param edge the edge to remove.
     */
    void removeEdge(ICjEdge edge);

}

