package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
class Link {

    private ReferNode refer;
    private String idTarget;
    private String linkLabel;
    private String target;
    private String id;
    private String label;


    public PathBuilder.PaarValue<Object> path(String... parent) {
        if (ReferNode.existing.equals(refer)) {
            PathBuilder.Existing existing = new PathBuilder.Existing();
            if (this.linkLabel != null)
                existing.edgeAttribute = new PathBuilder.PaarValue<>(linkLabel, String.join("", parent) + Arrays.stream(linkLabel.split("\\.")).collect(Collectors.joining("']['", "['", "']")));
            existing.edgeTarget = new PathBuilder.PaarValue<>(idTarget, String.join("", parent) + Arrays.stream(idTarget.split("\\.")).collect(Collectors.joining("']['", "['", "']")));
            return new PathBuilder.PaarValue<>(refer.name(), existing);

        } else if (ReferNode.inline.equals(refer)) {
            PathBuilder.Inline inline = new PathBuilder.Inline();
            if (this.linkLabel != null)
                inline.edgeAttribute = new PathBuilder.PaarValue<>(linkLabel, String.join("", parent) + Arrays.stream(linkLabel.split("\\.")).collect(Collectors.joining("']['", "['", "']")));
            if (this.label != null)
                inline.targetNodeAttribute = new PathBuilder.PaarValue<>(label, String.join("", parent) + Arrays.stream(label.split("\\.")).collect(Collectors.joining("']['", "['", "']")));
            if (this.target != null)
                inline.targetNodes = new PathBuilder.PaarValue<>(target, String.join("", parent) + Arrays.stream(target.split("\\.")).collect(Collectors.joining("']['", "['", "']")));
            if (this.id != null)
                inline.targetNodeId = new PathBuilder.PaarValue<>(id, String.join("", parent) + Arrays.stream(id.split("\\.")).collect(Collectors.joining("']['", "['", "']")));
            return new PathBuilder.PaarValue<>(refer.name(), inline);

        }
        return null;
    }

}
