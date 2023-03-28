package com.calpano.graphinout.reader.graphml;

public interface GraphmlEntity<E> {
    default void addCharacters(String characters) {
        String tmp = characters.replaceAll("\n", "");
        if (tmp.length() != 0) {
            throw new UnsupportedOperationException("No characters expected in <" + getName() + "> to " + this.getClass().getName());
        }
    }

    default void addEntity(GraphmlEntity<?> graphmlEntity) {
        throw new UnsupportedOperationException();
    }

    E getEntity();

    String getName();

    boolean isSent();

    void markAsSent();

    /** if this buffer entity is finally rendered via GioWriter to a graphml element of given name */
    default boolean resultsInGraphmlElement(String graphmlElementName) {
        return getName().equals(graphmlElementName);
    }
}
