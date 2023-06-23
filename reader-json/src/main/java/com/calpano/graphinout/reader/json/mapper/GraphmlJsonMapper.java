package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
 class GraphmlJsonMapper implements PathBuilder {

    private String id;
    private String label;
    private Set<Link> links;

    @Override
    public String findAll() {
        return "$[*]";
    }
    @Override
    public String findAllId() {
        return "$[*]['"+this.id+"']";
    }
    @Override
    public String findById(String id) {
        return "$[?(@."+this.id+" == '"+id+"')]";
    }

    @Override
    public String findById(Integer id) {
        return "$[?(@.id == "+id+")]";
    }

    @Override
    public String findByLabel(String label) {
        return "$[?(@."+this.label+" == '"+label+"')]";
    }

    @Override
    public Set<String> findLink(String id) {
        Set<String> tmpPaths = new HashSet<>();
        if(links!=null)
            links.forEach(link -> tmpPaths.add(link.path(findById(id))));
        return tmpPaths;
    }

    @Override
    public Set<String> findLink(Integer id) {
        Set<String> tmpPaths = new HashSet<>();
        if(links!=null)
            links.forEach(link -> tmpPaths.add(link.path(findById(id))));
        return tmpPaths;
    }
}
