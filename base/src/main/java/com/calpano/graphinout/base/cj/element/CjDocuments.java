package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.foundation.json.path.IJsonNavigationPath;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import java.util.HashMap;
import java.util.Map;

public class CjDocuments {

    /**
     * Inspects all core elements (doc, graph, node, edge, port, endpoint, labelEntry) which may have JSON data and
     * calculates a unified schema of effectively used types.
     */
    public static class CjDataSchema {

        private final Map<CjType, JsonTypeAnalysis> map = new HashMap<>();

        public void index(CjType cjType, ICjData data) {
            map.putIfAbsent(cjType, new JsonTypeAnalysis());
            map.get(cjType).index(data.jsonValue());
        }
    }

    public static final Object ANY = new Object() {
      public String toString() {
          return "ANY";
      }
    };

    /**
     * Use Java Map & List to model structure.
     *
     * null is undefined. {@link #ANY} is any.
     */
    public static class JsonTypeAnalysis {

        private Map<IJsonNavigationPath, IJsonPrimitive> map = new HashMap<>();

        public void index(IJsonValue data) {
            data.forEachLeaf( (path,primitive) -> {});
        }

    }


    static CjDataSchema calcEffectiveSchemaForData(ICjDocument document) {
        CjDataSchema schema = new CjDataSchema();
        document.allElements().forEach(elem -> {
            if (elem instanceof ICjHasData hasData) {
                schema.index(
                        elem.cjType(), hasData.data());
            }
        });
        return schema;
    }

}
