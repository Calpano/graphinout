package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.foundation.json.value.IJsonValue;

public interface ICjDataMutable extends ICjData {

    /** set the given value as the current state */
    void jsonNode(IJsonValue jsonValue);

}
