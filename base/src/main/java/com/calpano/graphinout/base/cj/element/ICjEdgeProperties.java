package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjEdgeType;

import javax.annotation.Nullable;

public interface ICjEdgeProperties extends ICjWithId {

    @Nullable
    CjEdgeType edgeType();

}
