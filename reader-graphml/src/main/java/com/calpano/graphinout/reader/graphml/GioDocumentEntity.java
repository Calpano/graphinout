package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioKey;

import java.util.ArrayList;

public class GioDocumentEntity implements GraphmlEntity<GioDocument>{
    private final GioDocument gioDocument ;

    public GioDocumentEntity(GioDocument gioDocument) {
        this.gioDocument = gioDocument;
    }

    @Override
    public GioDocument getEntity() {
        return gioDocument;
    }

    @Override
    public String getName() {
        return GraphmlConstant.GRAPHML_ELEMENT_NAME;
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
      if(GraphmlConstant.DESC_ELEMENT_NAME.equals(graphmlEntity.getName()))
          gioDocument.setDescription(((GioDescriptionEntity) graphmlEntity).getEntity().getDescription());
      else if (GraphmlConstant.KEY_ELEMENT_NAME.equals(graphmlEntity.getName())) {
          if(gioDocument.getKeys()==null)
              gioDocument.setKeys(new ArrayList<>());
          gioDocument.getKeys().add((GioKey)graphmlEntity.getEntity());
      } else if (GraphmlConstant.NODE_DATA_ELEMENT_NAME.equals(graphmlEntity.getName())) {
          if(gioDocument.getDataList()==null)
              gioDocument.setDataList(new ArrayList<>());
          gioDocument.getDataList().add((GioData)graphmlEntity.getEntity());
      }else{
          throw new RuntimeException("Graphml has not "+graphmlEntity.getName()+" element.");
      }
    }

    @Override
    public void addData(String data) {

    }

    @Override
    public boolean mustSendToStream(String newElementName) {
        return GraphmlConstant.GRAPH_ELEMENT_NAME.equals(newElementName);
    }
}
