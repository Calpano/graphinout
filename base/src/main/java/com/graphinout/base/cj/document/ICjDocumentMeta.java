package com.graphinout.base.cj.document;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ICjDocumentMeta extends ICjElement {

    @Nullable
    Boolean canonical();

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.empty();
    }

    @Nullable
    String versionDate();

    @Nullable
    String versionNumber();

}
