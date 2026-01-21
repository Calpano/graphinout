package com.graphinout.base.cj.document;

import com.graphinout.base.cj.writer.ICjWriter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * The part of a CJ node which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjNodeChunk extends ICjHasId, ICjHasData, ICjHasLabel, ICjHasPorts {

    default void fireStartChunk(ICjWriter cjWriter) {
        cjWriter.nodeStart();
        // streaming order: id, label, types, ports, data (graphs)
        cjWriter.maybe(id(), cjWriter::id);
        fireLabelMaybe(cjWriter);
        // Note: use nodeType() not edgeType() for node types
        cjWriter.list(types().toList(), CjType.ArrayOfNodeTypes, (type, writer) -> writer.nodeType(type));
        cjWriter.list(ports().toList(), CjType.ArrayOfPorts, ICjPort::fire);
        fireDataMaybe(cjWriter);
    }

    default ICjLabel label_() {
        return Objects.requireNonNull(label());
    }

    /**
     * Node types, 0..n types. Each type is like an edge type.
     */
    Stream<ICjElementType> types();

}
