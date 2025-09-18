package com.calpano.graphinout.base.cj.element;

public interface ICjGraphMetaMutable extends ICjGraphMeta {

    void canonical(Boolean canonical);

    void edgeCountInGraph(Long edgeCountInGraph);

    void edgeCountTotal(Long edgeCountTotal);

    void nodeCountInGraph(Long nodeCountInGraph);

    void nodeCountTotal(Long nodeCountTotal);

}
