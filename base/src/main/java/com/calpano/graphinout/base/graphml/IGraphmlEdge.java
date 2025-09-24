package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlEdgeBuilder;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.calpano.graphinout.foundation.text.StringFormatter.toStringOrNull;

public interface IGraphmlEdge extends IGraphmlElementWithDescAndId {

    String ATTRIBUTE_DIRECTED = "directed";
    String ATTRIBUTE_SOURCE = "source";
    String ATTRIBUTE_SOURCE_PORT = "sourceport";
    String ATTRIBUTE_TARGET = "target";
    String ATTRIBUTE_TARGET_PORT = "targetport";

    static GraphmlEdgeBuilder builder() {
        return new GraphmlEdgeBuilder();
    }

    /**
     * This is an attribute that can be true or false or null ot empty. The optional XML-Attribute directed declares if
     * the edge is directed or undirected. The value true declares a directed edge, the value false an undirected edge.
     * If the direction is not explicitly defined, the default direction is applied to this edge as defined in the
     * enclosing graph.
     * </p>
     * The name of this attribute in graph is <b>directed</b>
     */
    @Nullable
    Boolean directed();

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_DIRECTED, toStringOrNull(this::directed));
        name_value.accept(ATTRIBUTE_SOURCE, this::source);
        name_value.accept(ATTRIBUTE_SOURCE_PORT, this::sourcePort);
        name_value.accept(ATTRIBUTE_TARGET, this::target);
        name_value.accept(ATTRIBUTE_TARGET_PORT, this::targetPort);
    }

    @Nullable
    String id();

    String source();

    @Nullable
    String sourcePort();

    @Override
    default String tagName() {
        return GraphmlElements.EDGE;
    }

    String target();

    @Nullable
    String targetPort();

}
