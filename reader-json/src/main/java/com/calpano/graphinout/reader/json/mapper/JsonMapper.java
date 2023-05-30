package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
 class JsonMapper {

    private String id;
    private String label;
    private Set<Link> links;

    public Set<String> paths(){
        Set<String> tmpPaths = new HashSet<>();
        if(links!=null)
            links.forEach(link -> tmpPaths.add(link.path("$",id)));
        return tmpPaths;
    }

}
