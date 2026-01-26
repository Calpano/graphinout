package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a node in the CJ model, which may have ports and embedded subgraphs, plus associated data/labels.
 */
public interface ICjNode extends ICjNodeChunk, ICjHasGraphs, ICjCoreElement, Comparable<ICjNode> {

    /**
     * Compare first by chunk properties, then by graph arrays
     *
     * @param other
     * @return
     */
    @Override
    default int compareTo(@NonNull ICjNode other) {
        return Comparables.<ICjNode>comparing() //
                .byComparator(ICjNodeChunk::compare).byStream(ICjHasGraphs::graphs) //
                .compare(this, other);
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data().ifNotEmpty()).filter(Objects::nonNull), ports()), graphs());
    }

    /** Index of subGraph in this node @return -1 if not found */
    int indexOf(ICjGraph subGraph);

    @Override
    @NonNull ICjGraph parent();

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap().putMaybe(CjConstants.ID, id()).putMaybe(CjConstants.LABEL, label(), ICjLabel::toJaJsonMap).putMaybe(CjConstants.PORTS, ports(), ICjPort::toJaJsonMap).putMaybe(CjConstants.DATA, data(), ICjData::toJaJsonValue).putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap).build();
    }

}
