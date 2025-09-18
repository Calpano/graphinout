package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjEndpointElement;

import javax.annotation.Nullable;

public interface ICjEndpointMutable extends ICjEndpoint {

    CjEndpointElement node(String node);

    CjEndpointElement port(@Nullable String port);

    void type(@Nullable String type);

    void typeNode(@Nullable String typeNode);

    void typeUri(@Nullable String typeUri);

}
