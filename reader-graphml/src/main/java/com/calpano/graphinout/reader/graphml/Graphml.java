package com.calpano.graphinout.reader.graphml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graphml {

    public static boolean isXmlChildElementWithIndependentGioWriterCall(String parent, String child) {
        Set<String> set = parent_child.get(parent);
        return set != null && set.contains(child);
    }

    static Map<String, Set<String>> parent_child;

    static {
        parent_child = new HashMap<>();
        parent_child.computeIfAbsent("graphml", k->new HashSet<>()).add("graph");
        parent_child.computeIfAbsent("graphml", k->new HashSet<>()).add("data");
        parent_child.computeIfAbsent("graph", k->new HashSet<>()).add("data");
        parent_child.computeIfAbsent("graph", k->new HashSet<>()).add("node");
        parent_child.computeIfAbsent("graph", k->new HashSet<>()).add("edge");
        parent_child.computeIfAbsent("graph", k->new HashSet<>()).add("hyperedge");
        parent_child.computeIfAbsent("node", k->new HashSet<>()).add("data");
        parent_child.computeIfAbsent("node", k->new HashSet<>()).add("port");
        parent_child.computeIfAbsent("node", k->new HashSet<>()).add("graph");
        parent_child.computeIfAbsent("port", k->new HashSet<>()).add("data");
        parent_child.computeIfAbsent("port", k->new HashSet<>()).add("port");
        parent_child.computeIfAbsent("edge", k->new HashSet<>()).add("data");
        parent_child.computeIfAbsent("edge", k->new HashSet<>()).add("graph");
        parent_child.computeIfAbsent("hyperedge", k->new HashSet<>()).add("data");
        parent_child.computeIfAbsent("hyperedge", k->new HashSet<>()).add("endpoint");
        parent_child.computeIfAbsent("hyperedge", k->new HashSet<>()).add("graph");
    }



}
