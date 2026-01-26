package com.graphinout.base.cj.document;

import com.graphinout.base.cj.document.impl.CjEdgeChunk;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
import static java.util.Optional.ofNullable;

/**
 * The part of a CJ edge which can be sent in one go. Memory requirements for all data in this chunk are expected to be
 * below 50 MB.
 */
public interface ICjEdgeChunk extends ICjHasId, ICjHasData, ICjHasLabel {

    /**
     * compare first by id, then by label, then by endpoints, then by data
     */
    static int compare(ICjEdgeChunk a, ICjEdgeChunk b) {
        return Comparables.<ICjEdgeChunk>comparing() //
                .byKey(ICjHasId::id) //
                .byKey(ICjEdgeChunk::label) //
                .byStream(ICjEdgeChunk::endpoints) //
                .byKey(ICjEdgeChunk::data) //
                .compare(a, b);
    }

    default ICjEdgeChunkMutable copyMutable() {
        CjEdgeChunk copy = new CjEdgeChunk();
        copyTo(copy);
        return copy;
    }

    default void copyTo(ICjEdgeChunkMutable edge) {
        edge.id(id());
        ifPresentAccept(label(), l -> edge.labelMutable(l::copyTo));

        ifPresentAccept(edgeType(), edge::edgeType);
        endpoints().forEach(endpoint -> edge.addEndpoint(endpoint::copyTo));

        ifPresentAccept(data().jsonValue(), jsonValue -> edge.dataMutable(d -> d.setJsonValue(jsonValue)));
    }

    @Nullable ICjElementType edgeType();

    Stream<ICjEndpoint> endpoints();

    default void fireStartChunk(ICjWriter cjWriter, boolean sort) {
        cjWriter.edgeStart();
        // streaming order: id, label, type, endpoints, data, graphs
        cjWriter.maybe(id(), cjWriter::id);
        fireLabelMaybe(cjWriter,sort);
        ofNullable(edgeType()).ifPresent(cjWriter::edgeType);
        cjWriter.list(endpoints().toList(), CjType.ArrayOfEndpoints, sort, (iCjEndpoint, cjWriter1) -> iCjEndpoint.fire(cjWriter1, sort));
        fireDataMaybe(cjWriter);
    }

    /**
     * If this edge has exactly one endpoint with direction=IN, then this methods returns it. @throws
     * IllegalStateException if this edge has more than one source.
     */
    default @Nullable ICjEndpoint source() throws IllegalStateException {
        List<ICjEndpoint> sources = sources();
        if (sources.size() == 1) {
            return sources.getFirst();
        } else if (sources.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Edge has more than one source: " + this);
        }
    }

    default List<ICjEndpoint> sources() {
        return endpoints().filter(ICjEndpoint::isSource).toList();
    }

    default @Nullable ICjEndpoint target() throws IllegalStateException {
        List<ICjEndpoint> targets = targets();
        if (targets.size() == 1) {
            return targets.getFirst();
        } else if (targets.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Edge has more than one target: " + this);
        }
    }

    default List<ICjEndpoint> targets() {
        return endpoints().filter(ICjEndpoint::isTarget).toList();
    }

    default @Nullable String type() {
        return mapOrNull(edgeType(), ICjElementType::type);
    }

    default List<ICjEndpoint> undirectedEndpoints() {
        return endpoints().filter(ICjEndpoint::isUndirected).toList();
    }

}
