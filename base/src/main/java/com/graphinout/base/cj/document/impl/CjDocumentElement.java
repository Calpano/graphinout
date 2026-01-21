package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjDocumentMeta;
import com.graphinout.base.cj.document.ICjDocumentMetaMutable;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * A CJ document
 */
public class CjDocumentElement extends CjHasDataElement implements ICjDocumentMutable, ICjDocumentChunkMutable {

    /** All directed graphs in this document */
    private final List<CjGraphElement> graphs = new ArrayList<>();
    private @Nullable String baseUri;
    private @Nullable ICjDocumentMeta connectedJson;

    @Override
    public void addGraph(Consumer<ICjGraphMutable> graph) {
        CjGraphElement graphElement = new CjGraphElement(this);
        graph.accept(graphElement);
        graphs.add(graphElement);
    }

    @Nullable
    @Override
    public String baseUri() {
        return baseUri;
    }

    @Override
    public void baseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public CjType cjType() {
        return CjType.RootObject;
    }

    @Override
    public void connectedJson(ICjDocumentMeta meta) {
        if (connectedJson != null) throw new IllegalStateException("Meta already set");
        this.connectedJson = meta;
    }

    @Override
    public void connectedJson(Consumer<ICjDocumentMetaMutable> consumer) {
        CjDocumentMetaElement meta = new CjDocumentMetaElement();
        consumer.accept(meta);

        if (this.connectedJson == null) {
            this.connectedJson = meta;
        } else {
            // copy state
            CjDocumentMetaElement metaUnion = new CjDocumentMetaElement();
            ifPresentAccept(connectedJson.canonical(), metaUnion::canonical);
            ifPresentAccept(connectedJson.versionDate(), metaUnion::versionDate);
            ifPresentAccept(connectedJson.versionNumber(), metaUnion::versionNumber);
            ifPresentAccept(meta.canonical(), metaUnion::canonical);
            ifPresentAccept(meta.versionDate(), metaUnion::versionDate);
            ifPresentAccept(meta.versionNumber(), metaUnion::versionNumber);
            this.connectedJson = metaUnion;
        }
    }

    @Override
    @Nullable
    public ICjDocumentMeta connectedJson() {
        return connectedJson;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        fireStartChunk(cjWriter);
        cjWriter.list(graphs, CjType.ArrayOfGraphs, CjGraphElement::fire);
        cjWriter.documentEnd();
    }

    @Override
    public Stream<ICjGraph> graphs() {
        //noinspection RedundantCast
        return graphs.stream().map(x -> (ICjGraph) x);
    }

    @Override
    public int indexOf(ICjGraph graph) {
        //noinspection SuspiciousMethodCalls
        return graphs.indexOf(graph);
    }

    public String toCjJsonString() {
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        fire(cj2JsonWriter);
        return json2StringWriter.jsonString();
    }

}
