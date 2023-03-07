package com.calpano.graphinout.reader.graphml;

public abstract class AbstractGraphmlEntity<T> implements GraphmlEntity<T> {

    private boolean isSent = false;

    @Override
    public boolean isSent() {
        return isSent;
    }

    @Override
    public void markAsSent() {
        this.isSent = true;
    }


}
