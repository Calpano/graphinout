package com.calpano.graphinout.reader.json.mapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.annotation.Nonnull;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "refer")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = Link.LinkToExistingNode.class, name = "existing"), //
        @JsonSubTypes.Type(value = Link.LinkCreateNode.class, name = "inline") //
})
public class Link {

    /** the literal link label, not a JsonPointer */
    public String linkLabel;

    // Constructors
    public Link() {
    }

    public Link(String linkLabel) {
        this.linkLabel = linkLabel;
    }

    // Getters and Setters
    public String getLinkLabel() {
        return linkLabel;
    }

    public void setLinkLabel(String linkLabel) {
        this.linkLabel = linkLabel;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(linkLabel, link.linkLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkLabel);
    }

    @Override
    public String toString() {
        return "Link{" +
               "linkLabel='" + linkLabel + '\'' +
               '}';
    }

    public int compareTo(@Nonnull Link o) {
        return  this.getClass().getName().compareTo(o.getClass().getName()) ;
    }

    @JsonTypeName("existing")
    public static class LinkToExistingNode extends Link {
        public String idTarget;

        // Constructors
        public LinkToExistingNode() {
            super();
        }

        public LinkToExistingNode(String linkLabel, String idTarget) {
            super(linkLabel);
            this.idTarget = idTarget;
        }

        // Getters and Setters
        public String getIdTarget() {
            return idTarget;
        }

        public void setIdTarget(String idTarget) {
            this.idTarget = idTarget;
        }

        // equals, hashCode, toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            LinkToExistingNode that = (LinkToExistingNode) o;
            return Objects.equals(idTarget, that.idTarget);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), idTarget);
        }

        @Override
        public String toString() {
            return "LinkToExistingNode{" +
                   "idTarget='" + idTarget + '\'' +
                   ", linkLabel='" + linkLabel + '\'' +
                   '}';
        }

        public static LinkToExistingNode of(String linkLabel, String idTarget) {
            LinkToExistingNode link = new LinkToExistingNode();
            link.linkLabel = linkLabel;
            link.idTarget = idTarget;
            return link;
        }
    }

    @JsonTypeName("inline")
    public static class LinkCreateNode extends Link {
        public String target;
        public String id;
        public String label;

        // Constructors
        public LinkCreateNode() {
            super();
        }

        public LinkCreateNode(String linkLabel, String target, String id, String label) {
            super(linkLabel);
            this.target = target;
            this.id = id;
            this.label = label;
        }

        // Getters and Setters
        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

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

        // equals, hashCode, toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            LinkCreateNode that = (LinkCreateNode) o;
            return Objects.equals(target, that.target) &&
                   Objects.equals(id, that.id) &&
                   Objects.equals(label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), target, id, label);
        }

        @Override
        public String toString() {
            return "LinkCreateNode{" +
                   "target='" + target + '\'' +
                   ", id='" + id + '\'' +
                   ", label='" + label + '\'' +
                   ", linkLabel='" + linkLabel + '\'' +
                   '}';
        }

        public static LinkCreateNode of(String linkLabel, String target, String id, String label) {
            LinkCreateNode link = new LinkCreateNode();
            link.linkLabel = linkLabel;
            link.target = target;
            link.id = id;
            link.label = label;
            return link;
        }
    }
}
