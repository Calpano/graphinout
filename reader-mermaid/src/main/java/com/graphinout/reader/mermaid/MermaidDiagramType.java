package com.graphinout.reader.mermaid;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

public enum MermaidDiagramType {
    FLOWCHART, CLASS_DIAGRAM, STATE_DIAGRAM, ER_DIAGRAM, C4, MINDMAP, SANKEY, ARCHITECTURE;

    /** Detect the diagram type from a single content line (the first non-blank, non-comment, non-config line). */
    public static @Nullable MermaidDiagramType detect(String line) {
        String s = line.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("flowchart") || s.startsWith("graph ") || s.equals("graph")) return FLOWCHART;
        if (s.startsWith("classdiagram")) return CLASS_DIAGRAM;
        if (s.startsWith("statediagram")) return STATE_DIAGRAM;
        if (s.startsWith("erdiagram")) return ER_DIAGRAM;
        if (s.startsWith("c4context") || s.startsWith("c4container")
                || s.startsWith("c4component") || s.startsWith("c4dynamic")
                || s.startsWith("c4deployment")) return C4;
        if (s.startsWith("mindmap")) return MINDMAP;
        if (s.startsWith("sankey-beta") || s.equals("sankey")) return SANKEY;
        if (s.startsWith("architecture-beta") || s.startsWith("architecture")) return ARCHITECTURE;
        return null;
    }
}
