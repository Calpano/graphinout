package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.Set;

@Data
public class GraphmlJsonMapper {

    private String id;
    private String label;
    private Set<Link> links;

}
