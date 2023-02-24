package com.calpano.graphinout.reader.graphml;

import java.net.URL;

public class GioLocatorEntity implements GraphmlEntity<URL> {
    private final URL url;

    public GioLocatorEntity(URL url) {
        this.url = url;
    }

    @Override
    public void addData(String data) {

    }

    @Override
    public void addEntity(GraphmlEntity graphmlEntity) {

    }

    @Override
    public URL getEntity() {
        return url;
    }

    @Override
    public String getName() {
        return GraphmlConstant.LOCATOR_ELEMENT_NAME;
    }

    @Override
    public boolean mustSendToStream(String newElementName) {
        return false;
    }
}
