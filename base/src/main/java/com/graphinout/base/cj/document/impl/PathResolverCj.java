package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.ICjData;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.util.path.IMapLike;
import com.graphinout.foundation.util.path.PathResolver;

public class PathResolverCj {

    public static PathResolver createPathResolverCj() {
        PathResolver pathResolver = new PathResolver();
        pathResolver.registerMap(ICjData.class, data -> {
            IJsonValue jsonValue = data.jsonValue();
            if (jsonValue == null) return IMapLike.EMPTY;
            return jsonValue.asMapLike();
        });
        return pathResolver;
    }

}
