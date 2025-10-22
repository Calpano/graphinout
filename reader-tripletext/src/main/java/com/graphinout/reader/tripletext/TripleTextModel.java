package com.graphinout.reader.example;

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

    public void forEachTriple(ITripleHandler<Node, String, String> handler) {
        index.forEach(handler);
    }

    public Node indexNode(String id) {
        return nodes.computeIfAbsent(id, Node::new);
    }

    public void indexTriple(String s, String p, String o) {
        if (p.equals("label")) {
            Node node = indexNode(s);
            node.label = o;
            return;
        }
        index.index(indexNode(s), p, indexNode(o).id);
    }

    public Iterable<Node> nodes() {
        return nodes.values();
    }

}
