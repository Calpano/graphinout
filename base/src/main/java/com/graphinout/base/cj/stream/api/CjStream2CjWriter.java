package com.graphinout.base.cj.stream.api;

import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.element.ICjDocumentChunk;
import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunk;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunk;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjNodeChunk;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.element.impl.CjEdgeElement;
import com.graphinout.base.cj.element.impl.CjGraphElement;
import com.graphinout.base.cj.element.impl.CjNodeElement;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.foundation.util.PowerStackEnum;

public class CjStream2CjWriter implements ICjStream {

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
    enum Protocol {None, InNodes, InEdges, InGraphs}

    private final ICjWriter cjWriter;
    private final PowerStackEnum<Protocol> protocolStack = PowerStackEnum.create();

    public CjStream2CjWriter(ICjWriter cjWriter) {this.cjWriter = cjWriter;}

    @Override
    public ICjDocumentChunkMutable createDocumentChunk() {
        return new CjDocumentElement();
    }

    @Override
    public ICjEdgeChunkMutable createEdgeChunk() {
        return new CjEdgeElement();
    }

    @Override
    public ICjGraphChunkMutable createGraphChunk() {
        return new CjGraphElement();
    }

    @Override
    public ICjNodeChunkMutable createNodeChunk() {
        return new CjNodeElement();
    }

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
        if (protocolStack.peek() == Protocol.InGraphs) {
            protocolStack.pop(Protocol.InGraphs);
            cjWriter.listEnd(CjType.ArrayOfGraphs);
        }
        cjWriter.edgeEnd();
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        Protocol peek = protocolStack.peek();
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
            case InGraphs -> throw new IllegalStateException("Cannot get edge when in graphs.");
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
        // end current list
        switch (peek) {
            case None -> protocolStack.pop(Protocol.None);
            case InNodes -> {
                protocolStack.pop(Protocol.InNodes);
                cjWriter.listEnd(CjType.ArrayOfNodes);
            }
            case InEdges -> {
                protocolStack.pop(Protocol.InEdges);
                cjWriter.listEnd(CjType.ArrayOfEdges);
            }
        }
        // start new list
        switch (peek) {
            case None, InNodes, InEdges -> {
                // start graphs list
                cjWriter.listStart(CjType.ArrayOfGraphs);
                protocolStack.push(Protocol.InGraphs);
            }
            case InGraphs -> { /* good */ }
            default -> throw new IllegalStateException("Unexpected protocol: " + peek);
        }
        // state in new graph
        protocolStack.push(Protocol.None);

        graph.fireStartChunk(cjWriter);
    }

    @Override
    public void nodeEnd() {
        cjWriter.nodeEnd();
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        switch (protocolStack.peek()) {
            case None -> { // start nodes list
                cjWriter.listStart(CjType.ArrayOfNodes);
                protocolStack.pop(Protocol.None);
                protocolStack.push(Protocol.InNodes);
            }
            case InNodes -> { // perfect
            }
            case InEdges -> throw new IllegalStateException("Cannot get node when in edges.");
            case InGraphs -> throw new IllegalStateException("Cannot get node when in graphs.");
            default -> throw new IllegalStateException("Unexpected protocol: " + protocolStack.peek());
        }
        node.fireStartChunk(cjWriter);
    }

    private void maybeEndOpenList() {
        Protocol protocol = protocolStack.pop();
        switch (protocol) {
            case InEdges -> cjWriter.listEnd(CjType.ArrayOfEdges);
            case InNodes -> cjWriter.listEnd(CjType.ArrayOfNodes);
            case InGraphs -> cjWriter.listEnd(CjType.ArrayOfGraphs);
            case None -> { // empty doc is ok
            }
            default -> throw new IllegalStateException("Unexpected protocol: " + protocol);
        }
    }

}
