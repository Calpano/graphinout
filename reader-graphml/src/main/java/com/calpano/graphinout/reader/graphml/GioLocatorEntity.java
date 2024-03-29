package com.calpano.graphinout.reader.graphml;

import java.net.URL;

public class GioLocatorEntity extends AbstractGraphmlEntity<URL> implements GraphmlEntity<URL>{
    private final URL url;

    public GioLocatorEntity(URL url) {
        this.url = url;
    }

    @Override
    public URL getEntity() {
        return url;
    }

    @Override
    public String getName() {
        return GraphmlElement.LOCATOR;
    }


}
