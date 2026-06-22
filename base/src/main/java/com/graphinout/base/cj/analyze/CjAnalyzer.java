package com.graphinout.base.cj.analyze;

import com.graphinout.base.cj.document.ICjDocument;

import java.util.EnumSet;

/**
 * Inspects a CJ document and reports node/edge counts and the {@link CjFeature features} it uses.
 *
 * <p>Because every graphinout reader converts its input into the CJ model, this works as a uniform "inspect any graph
 * file" capability: read the file into an {@link ICjDocument}, then call {@link #analyze(ICjDocument)}.
 */
public final class CjAnalyzer {

    private CjAnalyzer() {}

    public static CjAnalysis analyze(ICjDocument doc) {
        long graphCount = CjFeature.allGraphs(doc).count();
        long nodeCount = CjFeature.allNodes(doc).count();
        long edgeCount = CjFeature.allEdges(doc).count();
        EnumSet<CjFeature> features = EnumSet.noneOf(CjFeature.class);
        for (CjFeature f : CjFeature.values()) {
            if (f.isPresentIn(doc)) {
                features.add(f);
            }
        }
        return new CjAnalysis(graphCount, nodeCount, edgeCount, features);
    }
}
