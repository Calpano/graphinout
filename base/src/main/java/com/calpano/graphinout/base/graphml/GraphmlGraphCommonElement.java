package com.calpano.graphinout.base.graphml;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// IMPROVE split into GraphmlElementWithData and GraphmlElementWithDesc
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GraphmlGraphCommonElement extends GraphmlElement {
    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected GraphmlDescription desc;

    /**
     * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to
     * the whole collection of graphs described by the content of <graphml>. These functions are declared by <key> elements
     * (children of <graphml>) and defined by <data> elements.
     * Occurence: <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
     *
     * @see GraphmlKey {@link GraphmlKey}
     * This is a list of elements that can be empty or null.
     * </p>
     * The name of this element is <b>data</b>
     */
    protected @Nullable List<GraphmlData> dataList;

    public void addData(String key, String data) {
        addData(GraphmlData.builder().key(key).value(data).build());
    }

    public void addData(GraphmlData data) {
        if (dataList == null)
            dataList = Collections.emptyList();
        dataList.add(dataList.size(), data);
    }



}
