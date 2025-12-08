package com.graphinout.base;

import com.graphinout.base.input.SingleInputSourceOfString;
import io.github.classgraph.Resource;

import java.io.IOException;

public class TestFileUtil2 {

    public static SingleInputSourceOfString inputSource(Resource resource) throws IOException {
        return SingleInputSourceOfString.of(resource.getURI().toString(), resource.getContentAsString());
    }

}
