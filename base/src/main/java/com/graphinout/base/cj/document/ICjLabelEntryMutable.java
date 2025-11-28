package com.graphinout.base.cj.document;

import org.jspecify.annotations.Nullable;

public interface ICjLabelEntryMutable extends ICjHasDataMutable, ICjLabelEntry {

    void language(@Nullable String language);

    void value(String value);

}
