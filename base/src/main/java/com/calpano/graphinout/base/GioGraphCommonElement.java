package com.calpano.graphinout.base;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class GioGraphCommonElement {
    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected GioDescription desc;
    /**
     * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to
     * the whole collection of graphs described by the content of <graphml>. These functions are declared by <key> elements
     * (children of <graphml>) and defined by <data> elements.
     * Occurence: <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
     *
     * @see GioKey {@link GioKey}
     * This is a list of elements that can be empty or null.
     * </p>
     * The name of this element is <b>data</b>
     */
    protected List<GioData> dataList;

    public void addData(String key, String data) {
        addData(GioData.builder().key(key).value(data).build());
    }

    public void addData(GioData data) {
        if (dataList == null)
            dataList = Collections.EMPTY_LIST;
        dataList.add(dataList.size(), data);
    }
}