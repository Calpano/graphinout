package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.*;

@Data
 class GraphmlJsonMapper implements PathBuilder {

    private String id;
    private String label;
    private Set<Link> links;

    @Override
    public PaarValue<String> findAll() {
        return new PaarValue<>("*","$[*]");
    }
    @Override
    public PaarValue<String>  findAllId() {
        return new PaarValue<String>(this.id,"$[*]['"+this.id+"']['"+this.label+"']");
    }
    @Override
    public PaarValue<String> findById(String id) {
        return new PaarValue<String>(this.id,"$[?(@."+this.id+" == '"+id+"')]");
    }

    @Override
    public PaarValue<String> findById(Integer id) {
        return new PaarValue<String>(this.id,"$[?(@.id == "+id+")]");
    }

    @Override
    public PaarValue<String> findByLabel(String label) {
        return new PaarValue<String>(this.id,"$[?(@."+this.label+" == '"+label+"')]");
    }

    @Override
    public Set<PaarValue> findLink(String id) {
        Set<PaarValue> tmpPaths = new TreeSet<>();
        if(links!=null)
            links.forEach(link -> tmpPaths.add(link.path(findById(id).path)));
        return tmpPaths;
    }

    @Override
    public Set<PaarValue<?>> findLink(Integer id) {
        Set<PaarValue<?>> tmpPaths = new HashSet<>();
        if(links!=null)
            links.forEach(link -> tmpPaths.add(link.path(findById(id).path)));
        return tmpPaths;
       }
}
