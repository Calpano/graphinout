package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.gio.GraphmlData;
import com.calpano.graphinout.base.gio.GraphmlDescription;
import com.calpano.graphinout.base.gio.GraphmlKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GraphmlGraphCommonElement {
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
    protected List<GraphmlData> dataList;

    public void addData(String key, String data) {
        addData(GraphmlData.builder().key(key).value(data).build());
    }

    public void addData(GraphmlData data) {
        if (dataList == null)
            dataList = Collections.EMPTY_LIST;
        dataList.add(dataList.size(), data);
    }


}