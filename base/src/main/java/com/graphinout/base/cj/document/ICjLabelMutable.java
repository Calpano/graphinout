package com.graphinout.base.cj.document;

import java.util.function.Consumer;

/**
 * Represents an array of {@link ICjLabelEntry}
 */
public interface ICjLabelMutable extends ICjLabel, ICjHasDataMutable {

    /** as many as you like */
    void addEntry(Consumer<ICjLabelEntryMutable> labelEntry);

    default void addEntry(ICjLabelEntry entry) {
        addEntry(entry::copyTo);
    }

    void removeEntry(ICjLabelEntry entry);

}
