package com.graphinout.reader.graphml;

import com.graphinout.base.graphml.GraphmlElements;

public class GioDefaultEntity extends AbstractGraphmlEntity<StringBuilder> implements GraphmlEntity<StringBuilder> {

    private StringBuilder builder;

    public GioDefaultEntity() {
        this.builder = new StringBuilder();
    }

    @Override
    public void addCharacters(String characters) {
        builder.append(characters);
    }

    @Override
    public StringBuilder getEntity() {
        return builder;
    }

    @Override
    public String getName() {
        return GraphmlElements.DEFAULT;
    }

}
