package com.calpano.graphinout.reader.json.mapper;

import java.util.EnumMap;
import java.util.Set;

public interface PathBuilder {

    String getId();

    String getLabel();
    PaarValue<String> findAll();

    PaarValue<String> findAllId();

    PaarValue<String> findById(String id);

    PaarValue<String> findById(Integer id);

    PaarValue<String> findByLabel(String label);

    Set<PaarValue> findLink(String id);

    Set<PaarValue<?>>  findLink(Integer id);

    class PaarValue<T> {
       public String name;
       public T path;

        public PaarValue(String name, T path) {
            this.name = name;
            this.path = path;
        }
    }

    class  Existing{
      public  PaarValue<String> edgeAttribute;
     public   PaarValue<String> edgeTarget;
    }
    class Inline{
       public PaarValue<String> edgeAttribute;
     public   PaarValue<String> targetNodes;
     public   PaarValue<String> targetNodeId;
      public  PaarValue<String> targetNodeAttribute;
    }

}
