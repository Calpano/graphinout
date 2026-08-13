package com.graphinout.base.cj.stream;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.factory.BaseCjOutput;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.collections.PowerStackEnum;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Locator;

import java.util.function.Consumer;

public class CjStream2CjWriter extends BaseCjOutput implements ICjStream {

    /**
     * The 'None' marker in the following protocol is a marker that the element was started but none of the expected
     * child types arrived yet.
     * <p>
     * Document: None, InGraphs
     * <p>
     * Graph: None, InNodes, InEdges, InGraphs
     */

    private final ICjWriter cjWriter;
    /** represent the current nesting */
    private final PowerStackEnum<CjType> protocolStack = PowerStackEnum.create();
    private final boolean sort;

    public CjStream2CjWriter(ICjWriter cjWriter, boolean sort) {
        this.cjWriter = cjWriter;
        this.sort = sort;
    }

    @Override
    public void documentEnd() {
        maybeNodesEnd();
        maybeEdgesEnd();
        maybeGraphsEnd();

        cjWriter.documentEnd();
        protocolStack.pop(CjType.RootObject);
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        document.fireStartChunk(cjWriter, sort);

        protocolStack.push(CjType.RootObject);
    }

    @Override
    public void edgeEnd() {
        maybeGraphsEnd();

        cjWriter.edgeEnd();
        protocolStack.pop(CjType.Edge);
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        maybeNodesEnd();
        maybeEdgesStart();

        edge.fireStartChunk(cjWriter, sort);
        protocolStack.push(CjType.Edge);
    }

    @Override
    public void graphEnd() {
        maybeNodesEnd();
        maybeEdgesEnd();
        maybeGraphsEnd();

        cjWriter.graphEnd();
        protocolStack.pop(CjType.Graph);
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        maybeNodesEnd();
        maybeEdgesEnd();
        maybeGraphsStart();

        graph.fireStartChunk(cjWriter, sort);
        protocolStack.push(CjType.Graph);
    }

    public void maybeEdgesEnd() {
        CjType type = protocolStack.peek();
        if (type == CjType.ArrayOfEdges) {
            cjWriter.listEnd(CjType.ArrayOfEdges);
            protocolStack.pop(CjType.ArrayOfEdges);
        }
    }

    public void maybeEdgesStart() {
        CjType type = protocolStack.peek();
        switch (type) {
            case Graph -> {
                // fine, adding edges to a graph
                cjWriter.listStart(CjType.ArrayOfEdges);
                protocolStack.push(CjType.ArrayOfEdges);
            }
            case ArrayOfEdges -> {
                // good, already in edges
            }
            default -> throw new IllegalStateException("Cannot start edges in " + type);
        }
    }

    /** @return true iff a graphs list was ended */
    public void maybeGraphsEnd() {
        CjType type = protocolStack.peek();
        if (type == CjType.ArrayOfGraphs) {
            cjWriter.listEnd(CjType.ArrayOfGraphs);
            protocolStack.pop(CjType.ArrayOfGraphs);
        }
    }

    public void maybeGraphsStart() {
        CjType type = protocolStack.peek();
        switch (type) {
            case RootObject -> {
                // root graph
                cjWriter.listStart(CjType.ArrayOfGraphs);
                protocolStack.push(CjType.ArrayOfGraphs);
            }
            case Graph, Node, Edge -> {
                // fine, adding graphs to a graph, node or edge
                cjWriter.listStart(CjType.ArrayOfGraphs);
                protocolStack.push(CjType.ArrayOfGraphs);
            }
            case ArrayOfGraphs -> {
                // good, already in graphs
            }
            default -> throw new IllegalStateException("Cannot start graphs in " + type);

        }
    }

    public void maybeNodesEnd() {
        CjType type = protocolStack.peek();
        if (type == CjType.ArrayOfNodes) {
            cjWriter.listEnd(CjType.ArrayOfNodes);
            protocolStack.pop(CjType.ArrayOfNodes);
        }
    }

    public void maybeNodesStart() {
        CjType type = protocolStack.peek();
        switch (type) {
            case Graph -> {
                // fine, adding nodes to a graph
                cjWriter.listStart(CjType.ArrayOfNodes);
                protocolStack.push(CjType.ArrayOfNodes);
            }
            case ArrayOfNodes -> {
                // good, already in nodes
            }
            default -> throw new IllegalStateException("Cannot start nodes in " + type);
        }
    }

    @Override
    public void nodeEnd() {
        // If a node had an open graphs-under-node list, close it before ending the node
        maybeGraphsEnd();

        cjWriter.nodeEnd();
        protocolStack.pop(CjType.Node);
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        // are we the first node?
        maybeNodesStart();

        node.fireStartChunk(cjWriter, sort);
        protocolStack.push(CjType.Node);
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        super.setContentErrorHandler(errorHandler);
        // chaining:
        cjWriter.setContentErrorHandler(errorHandler);
    }

    @Override
    public void setLocator(Locator locator) {
        super.setLocator(locator);
        // chaining:
        cjWriter.setLocator(locator);
    }


}
