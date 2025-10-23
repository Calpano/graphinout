package com.graphinout.base.cj.element;

import java.util.function.Consumer;

/**
 * Represents an array of {@link ICjLabelEntry}
 */
public interface ICjLabelMutable extends ICjLabel {

    /** as many as you like */
    void addEntry(Consumer<ICjLabelEntryMutable> labelEntry);

}
