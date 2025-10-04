package com.calpano.graphinout.foundation.json.path;

import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonTypeAnalysisTree {

    static class Node {
        int count = 0;
        Map<IJsonContainerNavigationStep,Node> children = new LinkedHashMap<>();
        Map<IJsonPrimitive, Integer> value_count = new LinkedHashMap<>();
    }

    Node root = new Node();

    public void index(IJsonNavigationPath path, IJsonPrimitive value) {
        Node current = root;
        for (IJsonContainerNavigationStep step : path.steps()) {
            current = current.children.computeIfAbsent(step, k -> new Node());
            current.count++;
        }
        current.value_count.merge(value, 1, Integer::sum);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(root, "", sb);
        return sb.toString();
    }

    private void toString(Node node, String indent, StringBuilder sb) {
        node.children.forEach((step, child) -> {
            sb.append(indent).append(step)
                    .append("#")
                    .append(child.count);

            if(child.count > 1) {
                sb.append("\n");
                toString(child, indent + "  ", sb);
            } else {
                toString(child, " ", sb);
            }
        });
        node.value_count.forEach((value, count) -> {
            sb.append(indent).append("  - ").append(value.toJsonString()).append("#").append(count).append("\n");
        });
    }

}
