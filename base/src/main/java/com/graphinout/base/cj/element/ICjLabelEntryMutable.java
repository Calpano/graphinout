package com.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjLabelEntryMutable extends ICjHasDataMutable, ICjLabelEntry {

    void language(@Nullable String language);

    void value(String value);

}
