package com.calpano.graphinout.reader.graphml;

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
        return GraphmlElement.DEFAULT;
    }

}
