package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjGraphMeta;

import javax.annotation.Nullable;

public class CjGraphMetaElement extends CjElement implements ICjGraphMeta {

    private Boolean canonical;
    private Long nodeCountTotal;
    private Long edgeCountTotal;
    private Long nodeCountInGraph;
    private Long edgeCountInGraph;

    CjGraphMetaElement(@Nullable CjElement parent) {
        super(parent);
    }


    public void canonical(Boolean canonical) {
        this.canonical = canonical;
    }

    @Nullable
    @Override
    public Boolean canonical() {
        return canonical;
    }

    @Override
    public CjType cjType() {
        return CjType.Meta;
    }

    public void edgeCountInGraph(Long edgeCountInGraph) {
        this.edgeCountInGraph = edgeCountInGraph;
    }

    @Nullable
    @Override
    public Long edgeCountInGraph() {
        return edgeCountInGraph;
    }

    public void edgeCountTotal(Long edgeCountTotal) {
        this.edgeCountTotal = edgeCountTotal;
    }

    @Nullable
    @Override
    public Long edgeCountTotal() {
        return edgeCountTotal;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        // alphabetic order
        cjWriter.metaStart();
        cjWriter.maybe(canonical, cjWriter::meta__canonical);
        cjWriter.maybe(edgeCountInGraph, cjWriter::meta__edgeCountInGraph);
        cjWriter.maybe(edgeCountTotal, cjWriter::meta__edgeCountTotal);
        cjWriter.maybe(nodeCountInGraph, cjWriter::meta__nodeCountInGraph);
        cjWriter.maybe(nodeCountTotal, cjWriter::meta__nodeCountTotal);
        cjWriter.metaEnd();
    }

    public void nodeCountInGraph(Long nodeCountInGraph) {
        this.nodeCountInGraph = nodeCountInGraph;
    }

    @Nullable
    @Override
    public Long nodeCountInGraph() {
        return nodeCountInGraph;
    }

    public void nodeCountTotal(Long nodeCountTotal) {
        this.nodeCountTotal = nodeCountTotal;
    }

    @Nullable
    @Override
    public Long nodeCountTotal() {
        return nodeCountTotal;
    }

}
