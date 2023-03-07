package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioGraph;

import javax.annotation.Nullable;
import java.net.URL;

public class GioGraphEntity extends AbstractGraphmlEntity<GioGraph> implements GraphmlEntity<GioGraph> {

    private final GioGraph gioGraph;
    public @Nullable URL url;


    public GioGraphEntity(GioGraph gioGraph) {
        this.gioGraph = gioGraph;
    }

    @Override
    public GioGraph getEntity() {
        return gioGraph;
    }

    @Override
    public String getName() {
        return GraphmlConstant.GRAPH_ELEMENT_NAME;
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
        if (graphmlEntity instanceof GioDescriptionEntity g) {
            gioGraph.setDescription(g.getEntity().getDescription());
        } else if (graphmlEntity instanceof GioDataEntity g) {
            // TODO rasul
            //if (gioGraph.getDataList() == null)
                //gioGraph.setDataList(new ArrayList<>());
            //gioGraph.getDataList().add(g.getEntity());
        } else {
            throw new RuntimeException("Graph has not " + graphmlEntity.getName() + " element.");
        }
    }

    @Override
    public void addData(String data) {

    }

}
