package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlElement;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDefaultBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDescriptionBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlLocatorBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlPortBuilder;
import com.calpano.graphinout.base.graphml.builder.IIdBuilder;
import com.calpano.graphinout.base.graphml.builder.ILocatorBuilder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Helper class to track element context */
public class Cj2GraphmlContext {

    final CjType cjType;
    /** will produce an {@link IGraphmlElement} or subtype thereof */
    private final GraphmlElementBuilder<?> builder;
    private final @Nullable Cj2GraphmlContext parent;
    private final Map<CjType, List<Cj2GraphmlContext>> children = new HashMap<>();
    private boolean isStarted;

    Cj2GraphmlContext(@Nullable Cj2GraphmlContext parent, CjType cjType, GraphmlElementBuilder<?> builder) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
        this.cjType = cjType;
        this.builder = builder;
        this.isStarted = false;
    }

    public IIdBuilder builderWithIdSupport() {
        IIdBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public ILocatorBuilder builderWithLocatorSupport() {
        ILocatorBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public void child1(CjType cjType, Consumer<Cj2GraphmlContext> consumer) {
        consumer.accept(child1(cjType));
    }

    public @Nullable Cj2GraphmlContext child1(CjType cjType) {
        List<Cj2GraphmlContext> list = children(cjType);
        assert list.size() <= 1;
        return list.isEmpty() ? null : list.getFirst();
    }

    public List<Cj2GraphmlContext> children(CjType cjType) {
        return children.getOrDefault(cjType, Collections.emptyList());
    }

    public GraphmlDataBuilder dataBuilder() {
        GraphmlDataBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlDefaultBuilder defaultBuilder() {
        GraphmlDefaultBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlDescriptionBuilder descBuilder() {
        GraphmlDescriptionBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlDocumentBuilder documentBuilder() {
        GraphmlDocumentBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlEndpointBuilder endpointBuilder() {
        GraphmlEndpointBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlGraphBuilder graphBuilder() {
        GraphmlGraphBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlHyperEdgeBuilder hyperEdgeBuilder() {
        GraphmlHyperEdgeBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public IIdBuilder idBuilder() {
        IIdBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public GraphmlKeyBuilder keyBuilder() {
        GraphmlKeyBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlLocatorBuilder locatorBuilder() {
        GraphmlLocatorBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    /**
     * Mark we wrote the START of this element to the downstream {@link GraphmlWriter}.
     */
    public void markAsStarted() {
        isStarted = true;
    }

    public void maybeWriteStartTo(GraphmlWriter graphmlWriter) throws IOException {
        if (isStarted()) return;
        if (parent != null) {
            parent.maybeWriteStartTo(graphmlWriter);
        }
        switch (cjType) {
            // FIXME decide when to emit doc start
            case CjType.RootObject -> graphmlWriter.documentStart(documentBuilder().build());
            case CjType.Graph -> graphmlWriter.graphStart(graphBuilder().build());
            case CjType.Node -> graphmlWriter.nodeStart(nodeBuilder().build());
            case CjType.Port -> graphmlWriter.portStart(portBuilder().build());
            case CjType.Edge -> {
                // TODO write edge or hyperedge - see another impl for that
                graphmlWriter.hyperEdgeStart(hyperEdgeBuilder().build());
                graphmlWriter.edgeStart(hyperEdgeBuilder().toEdge());
            }
            default -> throw new IllegalArgumentException("Cannot start " + cjType);
        }
        markAsStarted();
    }

    public GraphmlNodeBuilder nodeBuilder() {
        GraphmlNodeBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public GraphmlPortBuilder portBuilder() {
        GraphmlPortBuilder builder = builder();
        assert builder != null;
        return builder;
    }

    public void writeEndTo(GraphmlWriter graphmlWriter) throws IOException {
        maybeWriteStartTo(graphmlWriter);
        switch (cjType) {
            case CjType.RootObject -> graphmlWriter.documentEnd();
            case CjType.Graph -> graphmlWriter.graphEnd();
            case CjType.Node -> graphmlWriter.nodeEnd();
            case CjType.Port -> graphmlWriter.portEnd();
            case CjType.Edge -> {
                // TODO write edge or hyperedge - see another impl for that
                graphmlWriter.hyperEdgeEnd();
                graphmlWriter.edgeEnd();
            }
            default -> throw new IllegalArgumentException("Cannot end " + cjType);
        }
        markAsStarted();
    }

    private void addChild(Cj2GraphmlContext child) {
        children.compute(child.cjType, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(child);
            return v;
        });
    }

    private <T extends GraphmlElementBuilder<T>> T builder() {
        //noinspection unchecked
        return (T) builder;
    }

}
