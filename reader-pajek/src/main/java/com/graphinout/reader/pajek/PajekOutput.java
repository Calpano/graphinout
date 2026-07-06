package com.graphinout.reader.pajek;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PajekOutput {

    private record PajekEdge(int from, int to) {}

    private final ICjDocument cjDoc;

    public PajekOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    public String toPajek() {
        StringBuilder b = new StringBuilder();

        // Sort numerically when IDs are integers (Pajek round-trip), lexicographically otherwise (synthetic CJ)
        List<ICjNode> nodeList = cjDoc.nodesAllIncludingImplied()
                .sorted(Comparator.comparing(n -> n.id(), PajekOutput::compareIds))
                .toList();

        Map<String, Integer> idMap = new LinkedHashMap<>();
        for (int i = 0; i < nodeList.size(); i++) {
            idMap.put(nodeList.get(i).id(), i + 1);
        }

        b.append("*Vertices ").append(nodeList.size()).append("\n");
        for (ICjNode node : nodeList) {
            b.append(idMap.get(node.id()));
            String label = firstLabel(node.labelEntries());
            if (label != null) {
                b.append(" \"").append(label.replace("\"", "'")).append("\"");
            }
            b.append("\n");
        }

        List<PajekEdge> directed = new ArrayList<>();
        List<PajekEdge> undirected = new ArrayList<>();

        cjDoc.edgesAll().forEach(edge -> {
            ICjEndpoint inEp = edge.endpoints()
                    .filter(ep -> ep.direction() == CjDirection.IN)
                    .findFirst().orElse(null);
            ICjEndpoint outEp = edge.endpoints()
                    .filter(ep -> ep.direction() == CjDirection.OUT)
                    .findFirst().orElse(null);

            if (inEp != null && outEp != null) {
                Integer from = idMap.get(inEp.node());
                Integer to = idMap.get(outEp.node());
                if (from != null && to != null) {
                    directed.add(new PajekEdge(from, to));
                }
            } else {
                var eps = edge.endpoints().toList();
                if (eps.size() >= 2) {
                    Integer from = idMap.get(eps.get(0).node());
                    Integer to = idMap.get(eps.get(1).node());
                    if (from != null && to != null) {
                        undirected.add(new PajekEdge(from, to));
                    }
                }
            }
        });

        if (!directed.isEmpty()) {
            b.append("*Arcs\n");
            for (PajekEdge e : directed) {
                b.append(e.from()).append(" ").append(e.to()).append("\n");
            }
        }
        if (!undirected.isEmpty()) {
            b.append("*Edges\n");
            for (PajekEdge e : undirected) {
                b.append(e.from()).append(" ").append(e.to()).append("\n");
            }
        }

        return b.toString();
    }

    @Nullable
    private String firstLabel(List<ICjLabelEntry> labels) {
        if (labels.isEmpty()) return null;
        String val = labels.getFirst().value();
        return (val != null && !val.isEmpty()) ? val : null;
    }

    /** Numeric comparison when both strings are integers, lexicographic fallback. */
    private static int compareIds(String a, String b) {
        try {
            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }
}
