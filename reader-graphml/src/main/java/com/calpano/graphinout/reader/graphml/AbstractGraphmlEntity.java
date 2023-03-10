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

    protected void allowOnlyWhitespace(String characters) {
        if(characters.trim().length()==0) {
            // ok, just whitespace
        } else {
            throw new UnsupportedOperationException("No characters expected in "+getName()+". Got '"+characters+"'");
        }
    }



}
