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

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
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

    @Override
    public int hashCode() {
        return Objects.hash(id, label, links);
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
