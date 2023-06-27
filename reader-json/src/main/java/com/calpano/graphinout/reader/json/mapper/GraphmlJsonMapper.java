package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
class GraphmlJsonMapper implements PathBuilder {

    private String id;
    private String label;
    private Set<Link> links;

    @Override
    public PairValue<String> findAll() {
        return new PairValue<>("*", "$[*]");
    }

    @Override
    public PairValue<String> findAllId() {
        return new PairValue<String>(this.id, "$[*]['" + this.id + "']['" + this.label + "']");
    }

    @Override
    public PairValue<String> findById(String id) {
        return new PairValue<String>(this.id, "$[?(@." + this.id + " == '" + id + "')]");
    }

    @Override
    public PairValue<String> findById(Integer id) {
        return new PairValue<String>(this.id, "$[?(@.id == " + id + ")]");
    }

    @Override
    public PairValue<String> findByLabel(String label) {
        return new PairValue<String>(this.id, "$[?(@." + this.label + " == '" + label + "')]");
    }

    @Override
    public Set<PairValue<?>> findLink(String id) {
        Set<PairValue<?>> tmpPaths = new TreeSet<PairValue<?>>();
        if (links != null) links.forEach(link -> tmpPaths.add(link.path(findById(id).path)));
        return tmpPaths;
    }

    @Override
    public Set<PairValue<?>> findLink(Integer id) {
        Set<PairValue<?>> tmpPaths = new HashSet<>();
        if (links != null) links.forEach(link -> tmpPaths.add(link.path(findById(id).path)));
        return tmpPaths;
    }
}
