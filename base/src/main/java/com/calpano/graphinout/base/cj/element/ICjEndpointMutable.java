package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjDirection;

import javax.annotation.Nullable;

public interface ICjEndpointMutable extends ICjElement, ICjEndpoint, ICjHasDataMutable {

    ICjEndpointMutable direction(CjDirection direction);

    ICjEndpointMutable node(String node);

    ICjEndpointMutable port(@Nullable String port);

    ICjEndpointMutable type(@Nullable String type);

    ICjEndpointMutable typeNode(@Nullable String typeNode);

    ICjEndpointMutable typeUri(@Nullable String typeUri);

}
