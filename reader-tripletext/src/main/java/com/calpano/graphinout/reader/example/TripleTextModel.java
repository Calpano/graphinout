package com.calpano.graphinout.reader.example;

import java.util.HashMap;
import java.util.Map;

public class TripleTextModel {

    public static class Node {
        final String id;
        String label;

        public Node(String id) {
            this.id = id;
        }
    }


    private final TripleIndex<Node, String, String> index = new TripleIndex<>();
    private final Map<String, Node> nodes = new HashMap<>();

    public void forEachTriple(ITripleHandler<Node,String,String> handler) {
        index.forEach(handler);
    }

    public void indexTriple(String s, String p, String o) {
        if (p.equals("label")) {
            Node node = new Node(s);
            node.label = o;
            nodes.put(s, node);
            return;
        }
        index.index(nodes.computeIfAbsent(s, Node::new), p, o);
    }

    public Iterable<Node> nodes() {
        return nodes.values();
    }
}
