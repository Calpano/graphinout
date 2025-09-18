package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjEdgeType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public interface ICjEdge extends ICjElement, ICjHasGraphs, ICjHasId, ICjHasData, ICjHasLabel {

    @Nullable
    CjEdgeType edgeType();

    Stream<ICjEndpoint> endpoints();

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
