package com.graphinout.base.cj.stream;

import com.graphinout.base.cj.factory.BaseCjOutput;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.Locator;
import com.graphinout.foundation.util.PowerStackEnum;

import java.util.function.Consumer;

public class CjStream2CjWriter extends BaseCjOutput implements ICjStream {

    /**
     * The 'None' marker in the following protocol is a marker that the element was started but none of the expected
     * child types arrived ye.
     * <p>
     * Document: None, InGraphs
     * <p>
     * Graph: None, InNodes, InEdges, InGraphs
     * <p>
     * Call, resulting protocol stack:<br> {@link #documentStart(ICjDocumentChunk) docStart}, { None }<br>
     * {@link ICjWriter#listStart(CjType) graphs}, { InGraphs }<br> {@link #graphStart(ICjGraphChunk) graphStart}, {
     * InGraphs, None }<br> {@link ICjWriter#listStart(CjType) nodes}, { InGraphs, InNodes }<br>
     * {@link #nodeStart(ICjNodeChunk) nodeStart}, { InGraphs, InNodes }<br>
     * {@link ICjWriter#listStart(CjType) subgraphs}, { InGraphs, InNodes, None }<br>
     * {@link #edgeStart(ICjEdgeChunk) edgeStart}, { InGraphs, InNodes, InEdges }<br> ...
     */
    enum Protocol {None, InNodes, InEdges, InGraphsAtGraph, InGraphsAtNode}

    private final ICjWriter cjWriter;
    private final PowerStackEnum<Protocol> protocolStack = PowerStackEnum.create();


    public CjStream2CjWriter(ICjWriter cjWriter) {this.cjWriter = cjWriter;}

    @Override
    public void documentEnd() {
        maybeEndOpenList();
        cjWriter.documentEnd();
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        document.fireStartChunk(cjWriter);
        protocolStack.push(Protocol.None);
    }

    @Override
    public void edgeEnd() {
        // maybe end open graphs list
        Protocol peek = protocolStack.peek();
        if (peek == Protocol.InGraphsAtGraph) {
            protocolStack.pop(Protocol.InGraphsAtGraph);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
        } else if (peek == Protocol.InGraphsAtNode) {
            protocolStack.pop(Protocol.InGraphsAtNode);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
        }
        cjWriter.edgeEnd();
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        Protocol peek = protocolStack.peek();
        // Close any open graphs lists before starting edges
        if (peek == Protocol.InGraphsAtGraph) {
            protocolStack.pop(Protocol.InGraphsAtGraph);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
            peek = protocolStack.peek();
        } else if (peek == Protocol.InGraphsAtNode) {
            protocolStack.pop(Protocol.InGraphsAtNode);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
            peek = protocolStack.peek(); // likely InNodes under a node
        }
        switch (peek) {
            case None -> protocolStack.pop(Protocol.None);
            case InNodes -> {
                protocolStack.pop(Protocol.InNodes);
                cjWriter.listEnd(CjType.ArrayOfNodes);
            }
        }
        switch (peek) {
            case None, InNodes -> {
                // start edges list
                cjWriter.listStart(CjType.ArrayOfEdges);
                protocolStack.push(Protocol.InEdges);
            }
            case InEdges -> { /* good */ }
            case InGraphsAtGraph, InGraphsAtNode -> {
                // already handled above
            }
            default -> throw new IllegalStateException("Unexpected protocol: " + peek);
        }
        edge.fireStartChunk(cjWriter);
    }

    @Override
    public void graphEnd() {
        maybeEndOpenList();
        cjWriter.graphEnd();
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        Protocol peek = protocolStack.peek();
        // end current list only when appropriate
        switch (peek) {
            case None -> protocolStack.pop(Protocol.None);
            case InNodes -> { /* do NOT end nodes; graphs will be nested under node */ }
            case InEdges -> {
                protocolStack.pop(Protocol.InEdges);
                cjWriter.listEnd(CjType.ArrayOfEdges);
            }
            case InGraphsAtGraph, InGraphsAtNode -> { /* already in graphs list */ }
            default -> {}
        }
        // start new graphs list depending on context
        switch (peek) {
            case InNodes -> {
                // nested graphs inside current node
                cjWriter.listStart(CjType.ArrayOfGraphs);
                protocolStack.push(Protocol.InGraphsAtNode);
            }
            case None, InEdges -> {
                // graphs at graph level
                cjWriter.listStart(CjType.ArrayOfGraphs);
                protocolStack.push(Protocol.InGraphsAtGraph);
            }
            case InGraphsAtGraph, InGraphsAtNode -> { /* good: already in a graphs list */ }
            default -> {}
        }
        // state in new graph
        protocolStack.push(Protocol.None);

        graph.fireStartChunk(cjWriter);
    }

    @Override
    public void nodeEnd() {
        // If a node had an open graphs-under-node list, close it before ending the node
        Protocol peek = protocolStack.peek();
        if (peek == Protocol.InGraphsAtNode) {
            protocolStack.pop(Protocol.InGraphsAtNode);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
        }
        cjWriter.nodeEnd();
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        Protocol peek = protocolStack.peek();
        // If we are inside a graphs list, close it first
        if (peek == Protocol.InGraphsAtNode) {
            protocolStack.pop(Protocol.InGraphsAtNode);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
            peek = protocolStack.peek(); // should now be InNodes
        } else if (peek == Protocol.InGraphsAtGraph) {
            protocolStack.pop(Protocol.InGraphsAtGraph);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
            peek = protocolStack.peek(); // likely None
        }
        switch (peek) {
            case None -> { // start nodes list
                cjWriter.listStart(CjType.ArrayOfNodes);
                protocolStack.pop(Protocol.None);
                protocolStack.push(Protocol.InNodes);
            }
            case InNodes -> { // perfect
            }
            case InEdges -> throw new IllegalStateException("Cannot get node when in edges.");
            case InGraphsAtGraph, InGraphsAtNode -> throw new IllegalStateException("Cannot get node when in graphs.");
            default -> throw new IllegalStateException("Unexpected protocol: " + protocolStack.peek());
        }
        node.fireStartChunk(cjWriter);
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

    private void maybeEndOpenList() {
        Protocol protocol = protocolStack.pop();
        switch (protocol) {
            case InEdges -> cjWriter.listEnd(CjType.ArrayOfEdges);
            case InNodes -> cjWriter.listEnd(CjType.ArrayOfNodes);
            case InGraphsAtGraph, InGraphsAtNode -> cjWriter.listEnd(CjType.ArrayOfGraphs);
            case None -> { // empty doc is ok
            }
            default -> throw new IllegalStateException("Unexpected protocol: " + protocol);
        }
    }

}
