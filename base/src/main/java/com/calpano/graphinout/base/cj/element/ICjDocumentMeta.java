package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjDocumentMeta extends ICjElement {

    @Nullable
    Boolean canonical();

    @Nullable
    String versionDate();

    @Nullable
    String versionNumber();

}
