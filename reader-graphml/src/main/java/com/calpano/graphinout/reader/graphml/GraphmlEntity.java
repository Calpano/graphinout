package com.calpano.graphinout.reader.graphml;

public interface GraphmlEntity<E> {
    E getEntity();

    String getName();

    void addEntity(GraphmlEntity<?> graphmlEntity);

    void addData(String data);

    void markAsSent();

    boolean isSent();
}
