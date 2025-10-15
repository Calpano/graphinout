package com.graphinout.base.graphml;

import com.graphinout.base.graphml.builder.GraphmlGraphBuilder;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlGraph extends IGraphmlElementWithDescAndId {


    /**
     * Spec.html: "edgedefault (optional). The edgedefault attribute defines the default value of the edge attribute
     * directed. The default value for directed is directed."
     * <p>
     * DTD: "edgedefault (directed|undirected) #REQUIRED"
     * <p>
     * Our interpretation: null = directed
     */
    enum EdgeDefault {
        /** the default */
        directed, undirected;

        /** See {@link IGraphmlGraph#edgeDefault()} */
        public static final EdgeDefault DEFAULT_EDGE_DEFAULT = directed;

        public static boolean isDirected(@Nullable EdgeDefault edgeDefaultOrNull) {
            return edgeDefaultOrNull == null || edgeDefaultOrNull == EdgeDefault.directed;
        }

        public static boolean isDirected(@Nullable String edgeDefaultStringOrNull) {
            return edgeDefaultStringOrNull == null || edgeDefaultStringOrNull.equalsIgnoreCase(EdgeDefault.directed.graphmlString());
        }

        public String graphmlString() {
            return name();
        }
    }


    String ATTRIBUTE_EDGE_DEFAULT = "edgedefault";

    static GraphmlGraphBuilder builder() {
        return new GraphmlGraphBuilder();
    }

    /**
     * Spec.html: "edgedefault (optional). The edgedefault attribute defines the default value of the edge attribute
     * directed. The default value for directed is directed."
     * <p>
     * DTD: "edgedefault (directed|undirected) #REQUIRED"
     * <p>Our interpretation: null = directed
     */
    EdgeDefault edgeDefault();

    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_EDGE_DEFAULT, () -> edgeDefault().name());
    }

    @Nullable
    IGraphmlLocator locator();

    @Override
    default String tagName() {
        return GraphmlElements.GRAPH;
    }

}
