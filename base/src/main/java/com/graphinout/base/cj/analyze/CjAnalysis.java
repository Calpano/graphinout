package com.graphinout.base.cj.analyze;

import java.util.List;
import java.util.Set;

/**
 * Result of inspecting a CJ document: counts of graphs/nodes/edges (recursively, across all nesting levels) and the
 * set of {@link CjFeature graph-model features} the document exhibits.
 *
 * @see CjAnalyzer#analyze(com.graphinout.base.cj.document.ICjDocument)
 */
public record CjAnalysis(long graphCount, long nodeCount, long edgeCount, Set<CjFeature> features) {

    /** The present features as sorted registry slugs, e.g. {@code ["directed-edges", "node-labels"]}. */
    public List<String> featureSlugs() {
        return features.stream().map(CjFeature::slug).sorted().toList();
    }
}
