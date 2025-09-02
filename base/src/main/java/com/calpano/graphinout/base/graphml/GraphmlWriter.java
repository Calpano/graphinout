package com.calpano.graphinout.base.graphml;

import java.io.IOException;

/**
 * For large files, we don't want to keep the entire graph object in memory.
 * <p>
 * DESC is considered small and attached to data object. Also DESC is always FIRST in streaming order. DEFAULT value in
 * KEY same.
 */
public interface GraphmlWriter {

    void data(IGraphmlData data) throws IOException;

    void documentEnd() throws IOException;

    /**
     * start here.
     *
     * @param document includes {@link IGraphmlDescription} and {@link IGraphmlKey}
     */
    void documentStart(IGraphmlDocument document) throws IOException;

    void edgeEnd() throws IOException;

    /** @param edge contains {@link IGraphmlDescription}, node/port ids for source/target */
    void edgeStart(IGraphmlEdge edge) throws IOException;

    void graphEnd() throws IOException;

    /** @param graphmlGraph includes {@link IGraphmlDescription}, optional {@link IGraphmlLocator} */
    void graphStart(IGraphmlGraph graphmlGraph) throws IOException;

    void hyperEdgeEnd() throws IOException;

    /** @param edge includes {@link IGraphmlDescription}, {@link IGraphmlEndpoint} */
    void hyperEdgeStart(IGraphmlHyperEdge edge) throws IOException;

    /** @param data contains {@link IGraphmlDescription} */
    void key(IGraphmlKey data) throws IOException;

    void nodeEnd() throws IOException;

    /** @param node contains {@link IGraphmlDescription} and optional {@link IGraphmlLocator} */
    void nodeStart(IGraphmlNode node) throws IOException;

    void portEnd() throws IOException;

    /**
     * May contain <code>( PORT | DATA )*</code>.
     *
     * @param port contains {@link IGraphmlDescription}
     */
    void portStart(IGraphmlPort port) throws IOException;

}
