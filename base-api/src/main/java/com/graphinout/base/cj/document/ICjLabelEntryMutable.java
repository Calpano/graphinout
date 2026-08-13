package com.graphinout.base.cj.document;

import org.jspecify.annotations.Nullable;

/**
 * Mutable variant of {@link ICjLabelEntry}: allows setting value, language and data.
 */
public interface ICjLabelEntryMutable extends ICjHasDataMutable, ICjLabelEntry {

    void language(@Nullable String language);

    void value(String value);

}
