package com.calpano.graphinout.reader.json.mapper;

import java.util.Set;

public interface PathBuilder {

    class PairValue<T> {
        public String name;
        public T path;

        public PairValue(String name, T path) {
            this.name = name;
            this.path = path;
        }
    }

    class Existing {
        public PairValue<String> edgeAttribute;
        public PairValue<String> edgeTarget;
    }

    class Inline {
        public PairValue<String> edgeAttribute;
        public PairValue<String> targetNodes;
        public PairValue<String> targetNodeId;
        public PairValue<String> targetNodeAttribute;
    }

    PairValue<String> findAll();

    PairValue<String> findAllId();

    PairValue<String> findById(String id);

    PairValue<String> findById(Integer id);

    PairValue<String> findByLabel(String label);

    Set<PairValue<?>> findLink(String id);

    Set<PairValue<?>> findLink(Integer id);

    String getId();

    String getLabel();

}
