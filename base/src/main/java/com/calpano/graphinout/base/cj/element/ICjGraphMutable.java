package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjEdgeElement;
import com.calpano.graphinout.base.cj.element.impl.CjGraphMetaElement;
import com.calpano.graphinout.base.cj.element.impl.CjNodeElement;

import java.util.function.Consumer;

public interface ICjGraphMutable extends ICjGraph, ICjHasIdMutable, ICjHasGraphsMutable {

    void addEdge(Consumer<CjEdgeElement> edge);

    void addNode(Consumer<CjNodeElement> node);

    void meta(Consumer<CjGraphMetaElement> meta);

}
