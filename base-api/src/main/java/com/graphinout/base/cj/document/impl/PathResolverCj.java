package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.ICjData;
import com.graphinout.foundation.pure.collections.IMapLike;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.jvm.kpath.PathResolver;

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
