package com.calpano.graphinout.foundation.util.path;

import java.util.List;

public class KPaths {

    public static List<String> of(String pathString) {
        return List.of(pathString.split("/"));
    }

}
