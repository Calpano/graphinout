package com.graphinout.reader.graphml;

public interface GraphmlEntity<E> {

    default void addCharacters(String characters) {
        allowOnlyWhitespace(characters);
    }

    default void addEntity(GraphmlEntity<?> graphmlEntity) {
        throw new UnsupportedOperationException();
    }

    default void allowOnlyWhitespace(String characters) {
        if (characters.trim().length() == 0) {
            // ok, just whitespace & new lines
        } else {
            throw new UnsupportedOperationException("No characters '" + characters + "' expected in <" + getName() + "> to " + this.getClass().getName());
        }
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
