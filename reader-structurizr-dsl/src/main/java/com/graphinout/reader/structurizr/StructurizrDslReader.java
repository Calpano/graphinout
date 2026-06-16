package com.graphinout.reader.structurizr;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Element;
import com.structurizr.model.Model;
import com.structurizr.model.Relationship;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Reads a <a href="https://docs.structurizr.com/dsl">Structurizr DSL</a> workspace (a C4 architecture model) into the
 * Connected JSON model. Every model element (person, software system, container, component, deployment/infrastructure
 * node, ...) becomes a node; its kind is stored in {@code c4:kind} and its name is the label. Model relationships become
 * directed edges (label = relationship description, {@code c4:technology} = technology). The C4 containment hierarchy
 * (e.g. a container inside a software system) is emitted as {@code contains} edges from parent to child.
 *
 * <p>Parsing is delegated to the Apache-2.0-licensed {@link StructurizrDslParser}; views, styles and documentation in
 * the DSL are ignored (only the model graph is mapped).
 */
public class StructurizrDslReader implements GioReader {

    public static final String FORMAT_ID = "structurizr-dsl";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Structurizr DSL (C4 model)", ".dsl");

    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource sis = (SingleInputSource) inputSource;
        String content = IOUtils.toString(sis.inputStream(), StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return;
        }

        StructurizrDslParser parser = new StructurizrDslParser();
        Workspace workspace;
        try {
            parser.parse(content);
            workspace = parser.getWorkspace();
        } catch (Exception e) {
            // StructurizrDslParserException (and any unexpected parser failure) -> reported, not propagated
            reportError("Failed to parse Structurizr DSL: " + e.getMessage());
            return;
        }
        Model model = workspace.getModel();

        var doc = cjStream.createDocumentChunk();
        cjStream.documentStart(doc);
        var graph = cjStream.createGraphChunk();
        cjStream.graphStart(graph);

        // getElements()/getRelationships() return TreeSets ordered by id, so output is deterministic.
        for (Element element : model.getElements()) {
            var node = cjStream.createNodeChunk();
            node.id(element.getId());
            node.addProperty("c4:kind", element.getClass().getSimpleName());
            String name = element.getName();
            if (name != null && !name.isBlank()) {
                node.addLabelWithoutLanguage(name);
            }
            addIfPresent(node::addProperty, "c4:description", element.getDescription());
            addIfPresent(node::addProperty, "c4:technology", technologyOf(element));
            addIfPresent(node::addProperty, "c4:tags", element.getTags());
            cjStream.nodeStart(node);
            cjStream.nodeEnd();
        }

        // C4 containment as structural edges (parent -> child).
        for (Element element : model.getElements()) {
            Element parent = element.getParent();
            if (parent != null) {
                emitEdge(cjStream, parent.getId(), element.getId(), "contains", null, null);
            }
        }

        // Model relationships (source -> destination).
        for (Relationship relationship : model.getRelationships()) {
            String type = isBlank(relationship.getTechnology()) ? "relationship" : "uses";
            emitEdge(cjStream, relationship.getSourceId(), relationship.getDestinationId(), type,
                    relationship.getDescription(), relationship.getTechnology());
        }

        cjStream.graphEnd();
        cjStream.documentEnd();
    }

    private void emitEdge(ICjStream cjStream, String sourceId, String targetId, String type,
                          @Nullable String label, @Nullable String technology) {
        var edge = cjStream.createEdgeChunk();
        edge.addEndpoint(ep -> ep.node(sourceId).direction(CjDirection.IN));
        edge.addEndpoint(ep -> ep.node(targetId).direction(CjDirection.OUT));
        edge.edgeType(ICjElementType.of(type));
        edge.addProperty("c4:rel", type);
        if (label != null && !label.isBlank()) {
            edge.addLabelWithoutLanguage(label);
        }
        addIfPresent(edge::addProperty, "c4:technology", technology);
        cjStream.edgeStart(edge);
        cjStream.edgeEnd();
    }

    private static @Nullable String technologyOf(Element element) {
        if (element instanceof Container c) {
            return c.getTechnology();
        }
        if (element instanceof Component c) {
            return c.getTechnology();
        }
        return null;
    }

    private static void addIfPresent(java.util.function.BiConsumer<String, String> sink, String key,
                                     @Nullable String value) {
        if (!isBlank(value)) {
            sink.accept(key, value);
        }
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.isBlank();
    }

    private void reportError(String message) {
        if (errorHandler != null) {
            errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Error, message));
        }
    }

}
