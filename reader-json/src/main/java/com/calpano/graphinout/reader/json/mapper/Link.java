package com.calpano.graphinout.reader.json.mapper;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "refer")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = Link.LinkToExistingNode.class, name = "existing"), //
        @JsonSubTypes.Type(value = Link.LinkCreateNode.class, name = "inline") //
})
@Data
public class Link {


    public String linkLabel;


    public int compareTo(@NotNull Link o) {
        return  this.getClass().getName().compareTo(o.getClass().getName()) ;
    }

    @JsonTypeName("existing")
    @Data
    public static class LinkToExistingNode extends Link {
        public String idTarget;

        public static LinkToExistingNode of(String linkLabel, String idTarget) {
            LinkToExistingNode link = new LinkToExistingNode();
            link.linkLabel = linkLabel;
            link.idTarget = idTarget;
            return link;
        }
    }

    @JsonTypeName("inline")
    @Data
    public static class LinkCreateNode extends Link {
        public String target;
        public String id;
        public String label;

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
