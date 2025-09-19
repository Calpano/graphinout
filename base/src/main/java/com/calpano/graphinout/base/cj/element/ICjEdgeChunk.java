package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * The part of a CJ edge which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjEdgeChunk extends ICjHasData, ICjHasId, ICjHasLabel {

    @Nullable
    ICjEdgeType edgeType();

    Stream<ICjEndpoint> endpoints();

    default void fireStartChunk(ICjWriter cjWriter) {
        cjWriter.edgeStart();
        // streaming order: id, label, type, typeUri, typeNode, endpoints, data, graphs
        cjWriter.maybe(id(), cjWriter::id);
        fireLabelMaybe(cjWriter);
        ofNullable(edgeType()).ifPresent(cjWriter::edgeType);
        cjWriter.list(endpoints().toList(), CjType.ArrayOfEndpoints, ICjEndpoint::fire);
        fireDataMaybe(cjWriter);
    }

    /**
     * If this edge has exactly one endpoint with direction=IN, then this methods returns it. @throws
     * IllegalStateException if this edge has more than one source.
     */
    default @Nullable ICjEndpoint source() throws IllegalStateException {
        List<ICjEndpoint> sources = endpoints().filter(ICjEndpoint::isSource).toList();
        if (sources.size() == 1) {
            return sources.getFirst();
        } else if (sources.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Edge has more than one source: " + this);
        }
    }

    default @Nullable ICjEndpoint target() throws IllegalStateException {
        List<ICjEndpoint> targets = endpoints().filter(ICjEndpoint::isTarget).toList();
        if (targets.size() == 1) {
            return targets.getFirst();
        } else if (targets.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Edge has more than one target: " + this);
        }
    }

}
