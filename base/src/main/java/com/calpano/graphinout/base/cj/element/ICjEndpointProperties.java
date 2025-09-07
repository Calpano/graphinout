package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjDirection;

import javax.annotation.Nullable;

public interface ICjEndpointProperties {

    String node();

    @Nullable
    String port();

    @Nullable
    CjDirection direction();

    @Nullable
    String type();

    @Nullable
    String typeUri();

    @Nullable
    String typeNode();

}
