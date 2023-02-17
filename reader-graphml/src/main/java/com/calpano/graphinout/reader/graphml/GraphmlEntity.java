package com.calpano.graphinout.reader.graphml;

import java.util.Map;

public interface GraphmlEntity<E> {
    E getEntity();

    String getName();

    void addEntity(GraphmlEntity<?> graphmlEntity);

    void addData(String data);

    boolean  mustSendToStream(String newElementName);
}
