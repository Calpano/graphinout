package com.calpano.graphinout.reader.json.mapper;

import lombok.Data;

import java.util.*;
import java.util.function.Consumer;

@Data
public class GraphmlJsonMapping {

    private String id;
    private String label;
    private Set<Link> links;



}
