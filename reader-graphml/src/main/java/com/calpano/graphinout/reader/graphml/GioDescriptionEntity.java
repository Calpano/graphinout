package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioElementWithDescription;

public class GioDescriptionEntity extends AbstractGraphmlEntity<GioElementWithDescription>  implements GraphmlEntity<GioElementWithDescription>{

   private GioElementWithDescription gioElementWithDescription= new GioElementWithDescription() {
   };
    @Override
    public GioElementWithDescription getEntity() {
        return gioElementWithDescription;
    }

    @Override
    public String getName() {
        return GraphmlConstant.DESC_ELEMENT_NAME;
    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {
       throw  new RuntimeException("Description has not any inner element.");
    }

    @Override
    public void addData(String data) {
        gioElementWithDescription.setDescription(data);
    }

}
