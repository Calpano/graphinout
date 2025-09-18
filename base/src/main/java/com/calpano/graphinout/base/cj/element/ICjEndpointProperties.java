package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjDirection;

import javax.annotation.Nullable;

public interface ICjEndpointProperties {

    @Nullable
    CjDirection direction();

    default boolean isSource() {
        return direction() == CjDirection.IN;
    }

    default boolean isTarget() {
        return direction() == CjDirection.OUT;
    }

    String node();

    @Nullable
    String port();

    @Nullable
    String type();

    @Nullable
    String typeNode();

    @Nullable
    String typeUri();


}
