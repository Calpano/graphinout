package com.calpano.graphinout.reader.json.mapper;

import java.util.Objects;
import java.util.Set;

public class GraphmlJsonMapping {

    private String id;
    /** JsonPointer to the label text node within the data JSON node */
    private String label;
    private Set<Link> links;

    // Constructors
    public GraphmlJsonMapping() {
    }

    public GraphmlJsonMapping(String id, String label, Set<Link> links) {
        this.id = id;
        this.label = label;
        this.links = links;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphmlJsonMapping that = (GraphmlJsonMapping) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                Objects.equals(links, that.links);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Set<Link> getLinks() {
        return links;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, links);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "GraphmlJsonMapping{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", links=" + links +
                '}';
    }

}
