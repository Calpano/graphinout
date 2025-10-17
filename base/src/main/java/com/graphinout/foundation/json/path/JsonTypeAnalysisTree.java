package com.graphinout.foundation.json.path;

import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.value.IJsonPrimitive;
import com.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JsonTypeAnalysisTree {

    public static class Node {

        int count = 0;
        Map<IJsonContainerNavigationStep, Node> children = new LinkedHashMap<>();
        Map<IJsonPrimitive, Integer> value_count = new LinkedHashMap<>();

        public Map<IJsonContainerNavigationStep, Node> children() {
            return children;
        }

        /**
         * Which container type is required to represent the values? If object and array steps occur, an exception is
         * thrown. null represents that NO container is required because all values are primitives.
         */
        public @Nullable JsonType containerJsonType() {
            boolean containsObjectSteps = children.keySet().stream().anyMatch(s -> s instanceof IJsonArrayNavigationStep);
            boolean containsArraySteps = children.keySet().stream().anyMatch(s -> s instanceof IJsonObjectNavigationStep);
            if (containsObjectSteps && containsArraySteps)
                throw new IllegalStateException("Cannot have both object and array steps in the same container.");
            if (containsObjectSteps) return JsonType.Object;
            if (containsArraySteps) return JsonType.Array;
            return null;
        }

        public int count() {
            return count;
        }

        public Stream<IJsonPrimitive> distinctJsonPrimitiveValues() {
            return value_count.keySet().stream().distinct();
        }

        public boolean isMapOfPrimitives() {
            return children.isEmpty();
        }

        public Map<IJsonPrimitive, Integer> valueCounts() {
            return value_count;
        }

    }

    Node root = new Node();

    public Node get(IJsonContainerNavigationStep step) {
        return root.children.get(step);
    }

    public void index(IJsonValue value) {
        value.forEachLeaf(this::index);
    }

    public void index(IJsonNavigationPath path, IJsonPrimitive value) {
        Node current = root;
        for (IJsonContainerNavigationStep step : path.steps()) {
            current = current.children.computeIfAbsent(step, k -> new Node());
            current.count++;
        }
        current.value_count.merge(value, 1, Integer::sum);
    }

    public Set<IJsonContainerNavigationStep> rootSteps() {
        return root.children.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(root, "", sb);
        return sb.toString();
    }

    private void toString(Node node, String indent, StringBuilder sb) {
        node.children.forEach((step, child) -> {
            sb.append(indent).append(step).append("#").append(child.count);

            if (child.count > 1) {
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
