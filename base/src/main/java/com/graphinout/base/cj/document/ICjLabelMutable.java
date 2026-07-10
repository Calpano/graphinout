package com.graphinout.base.cj.document;

import java.util.function.Consumer;

/**
 * Represents an array of {@link ICjLabelEntry}
 */
public interface ICjLabelMutable extends ICjLabel, ICjHasDataMutable {

    /** 
     * Adds a new entry, customized via the provided consumer.
     *
     * @param labelEntry the consumer to configure the entry.
     */
    void addEntry(Consumer<ICjLabelEntryMutable> labelEntry);

    default void addEntry(ICjLabelEntry entry) {
        addEntry(entry::copyTo);
    }

    /**
     * Removes the specified entry from this label.
     *
     * @param entry the entry to remove.
     */
    void removeEntry(ICjLabelEntry entry);

}
