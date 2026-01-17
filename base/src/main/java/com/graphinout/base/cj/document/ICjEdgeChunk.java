package com.graphinout.base.cj.document;

import com.graphinout.base.cj.document.impl.CjEdgeElement;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static java.util.Optional.ofNullable;

/**
 * The part of a CJ edge which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjEdgeChunk extends ICjHasData, ICjHasId, ICjHasLabel {

    default ICjEdgeChunkMutable copyMutable() {
        CjEdgeElement copy = new CjEdgeElement();
        copyTo(copy);
        return copy;
    }

    default void copyTo(ICjEdgeChunkMutable edge) {
        edge.id(id());
        ifPresentAccept(label(), l -> edge.labelMutable(l::copyTo));

        ifPresentAccept(edgeType(), edge::edgeType);
        endpoints().forEach(endpoint -> edge.addEndpoint(endpoint::copyTo));

        ifPresentAccept(data(), ICjData::jsonValue, jsonValue -> edge.dataMutable(d -> d.setJsonValue(jsonValue)));

    }

    @Nullable ICjEdgeType edgeType();

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
            return sources.get(0);
        } else if (sources.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Edge has more than one source: " + this);
        }
    }

    default @Nullable ICjEndpoint target() throws IllegalStateException {
        List<ICjEndpoint> targets = endpoints().filter(ICjEndpoint::isTarget).toList();
        if (targets.size() == 1) {
            return targets.get(0);
        } else if (targets.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Edge has more than one target: " + this);
        }
    }

}
