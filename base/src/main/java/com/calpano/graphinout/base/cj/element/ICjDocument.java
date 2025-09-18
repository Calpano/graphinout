package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.List;

public interface ICjDocument extends ICjHasGraphs, ICjHasData {

    @Nullable
    ICjDocumentMeta connectedJson();

    @Nullable
    String baseUri();


}
