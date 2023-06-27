package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
class Link {

    private ReferType refer;
    private String idTarget;
    private String linkLabel;
    private String target;
    private String id;
    private String label;

    private static String jsonPath(String s, String... parents) {
        return String.join("", parents) + Arrays.stream(s.split("\\.")).collect(Collectors.joining("']['", "['", "']"));
    }

    /**
     *
     * @param parents
     * @return
     */
    public PathBuilder.PairValue<Object> path(String... parents) {
        if (ReferType.existing.equals(refer)) {
            PathBuilder.Existing existing = new PathBuilder.Existing();
            if (this.linkLabel != null)
                existing.edgeAttribute = new PathBuilder.PairValue<>(linkLabel, jsonPath(linkLabel,parents));
            existing.edgeTarget = new PathBuilder.PairValue<>(idTarget, jsonPath(idTarget,parents));
            return new PathBuilder.PairValue<>(refer.name(), existing);

        } else if (ReferType.inline.equals(refer)) {
            PathBuilder.Inline inline = new PathBuilder.Inline();
            if (this.linkLabel != null)
                inline.edgeAttribute = new PathBuilder.PairValue<>(linkLabel, jsonPath(linkLabel,parents));
            if (this.label != null) inline.targetNodeAttribute = new PathBuilder.PairValue<>(label, jsonPath(label,parents));
            if (this.target != null) inline.targetNodes = new PathBuilder.PairValue<>(target, jsonPath(target,parents));
            if (this.id != null) inline.targetNodeId = new PathBuilder.PairValue<>(id, jsonPath(id,parents));
            return new PathBuilder.PairValue<>(refer.name(), inline);
        }
        return null;
    }



}
