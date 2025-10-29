package com.graphinout.base.cj.document;

import javax.annotation.Nullable;

public interface ICjEndpointMutable extends ICjElement, ICjEndpoint, ICjHasDataMutable {

    ICjEndpointMutable direction(CjDirection direction);

    ICjEndpointMutable node(String node);

    ICjEndpointMutable port(@Nullable String port);

    ICjEndpointMutable type(@Nullable String type);

    ICjEndpointMutable typeNode(@Nullable String typeNode);

    ICjEndpointMutable typeUri(@Nullable String typeUri);

}
