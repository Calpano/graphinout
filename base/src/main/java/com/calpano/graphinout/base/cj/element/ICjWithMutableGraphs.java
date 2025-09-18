package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjGraphElement;

import java.util.function.Consumer;

public interface ICjWithMutableGraphs extends ICjWithGraphs {

    CjGraphElement addGraph(Consumer<CjGraphElement> consumer);

}
