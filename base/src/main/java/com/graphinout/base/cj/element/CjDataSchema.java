package com.graphinout.base.cj.element;

import com.graphinout.base.cj.CjType;
import com.graphinout.foundation.json.path.JsonTypeAnalysisTree;
import com.graphinout.foundation.json.value.IJsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Inspects all core elements (doc, graph, node, edge, port, endpoint, labelEntry) which may have JSON data and
 * calculates a unified schema of effectively used types.
 */
public class CjDataSchema {

    public Map<CjType, JsonTypeAnalysisTree> map() {
        return map;
    }

    private final Map<CjType, JsonTypeAnalysisTree> map = new HashMap<>();

    public void index(CjType cjType, ICjData data) {
        IJsonValue jsonValue = data.jsonValue();
        if (jsonValue == null) return;
        map.putIfAbsent(cjType, new JsonTypeAnalysisTree());
        map.get(cjType).index(jsonValue);
    }


}
