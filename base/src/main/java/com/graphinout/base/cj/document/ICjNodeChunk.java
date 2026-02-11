package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.util.Comparables;

import java.util.Objects;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * The part of a CJ node which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjNodeChunk extends ICjHasId, ICjHasData, ICjHasLabel, ICjHasPorts {

    /**
     * compare first by id, then by label, then by types, then by ports, then by data
     */
    static int compare(ICjNodeChunk a, ICjNodeChunk b) {
        return Comparables.<ICjNodeChunk>comparing() //
                .byKey(ICjHasId::id) //
                .byKey(ICjNodeChunk::label) //
                .byStream(ICjNodeChunk::types) //
                .byStream(ICjNodeChunk::ports) //
                .byKey(ICjNodeChunk::data) //
                .compare(a, b);
    }

    default void copyTo(ICjNodeMutable targetNode) {
        ifPresentAccept(id(), targetNode::id);
        ifPresentAccept(label(), label -> targetNode.labelMutable(label::copyTo));
        data(data -> targetNode.dataJsonValue(data.jsonValue()));
        ports().forEach(sourcePort -> targetNode.addPort(sourcePort::copyTo));
    }

    default void fireStartChunk(ICjWriter cjWriter, boolean sort) {
        cjWriter.nodeStart();
        // streaming order: id, label, types, ports, data (graphs)
        cjWriter.maybe(id(), cjWriter::id);
        fireLabelMaybe(cjWriter, sort);
        // Note: use nodeType() not edgeType() for node types
        cjWriter.list(types().toList(), CjType.ArrayOfNodeTypes, sort, (type, writer) -> writer.nodeType(type));
        cjWriter.list(ports().toList(), CjType.ArrayOfPorts, sort, (iCjPort, cjWriter1) -> iCjPort.fire(cjWriter1, sort));
        fireDataMaybe(cjWriter);
    }

    default ICjLabel label_() {
        return Objects.requireNonNull(label());
    }

    /**
     * Node types, 0..n types. Each type is like an edge type.
     */
    Stream<ICjElementType> types();

    /**
     * A hash based on label, types, ports, and data. Excludes id.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder();
        if (label() != null) {
            sb.append("L:").append(label().structuralHash());
        }
        sb.append("|T:");
        types().forEach(t -> sb.append(t.type()).append(","));
        sb.append("|P:");
        ports().forEach(p -> sb.append(p.structuralHash()).append(","));
        if (data() != null && !data().isEmpty()) {
            sb.append("|D:").append(data().hashCode());
        }
        return Integer.toString(sb.toString().hashCode());
    }

}
