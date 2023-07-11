package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.Set;

@Data
public class GraphmlJsonMapping {

    private String id;
    /** JsonPointer to the label text node within the data JSON node */
    private String label;
    private Set<Link> links;



}
