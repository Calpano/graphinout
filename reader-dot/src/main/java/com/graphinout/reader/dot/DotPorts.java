package com.graphinout.reader.dot;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Graphviz port semantics: which ports a node declares, and which names are compass points.
 * <p>
 * In DOT, {@code A:p} only means something if node {@code A} declares a port {@code p}: as a field {@code <p>} of a
 * {@code shape=record}/{@code Mrecord} label, or as a {@code PORT="p"} attribute in an HTML label. Otherwise, if
 * {@code p} is a compass point, it is read as one; a compass point is in effect a built-in port every node has.
 * Anything else is reported by Graphviz as "port unrecognized" and ignored.
 */
final class DotPorts {

    /** The compass points, case-sensitive, as Graphviz accepts them. {@code _} = any side, {@code c} = center. */
    static final List<String> COMPASS_POINTS = List.of("n", "ne", "e", "se", "s", "sw", "w", "nw", "c", "_");

    /**
     * Data key with a compass point: on a CJ port, marks it as that compass point; on an endpoint, the compass point
     * of an ignored port reference.
     */
    static final String DATA_COMPASS = "dot.compass";
    /** Endpoint data key holding a DOT port that Graphviz ignores (not declared, not a compass point). */
    static final String DATA_PORT = "dot.port";

    /** Label entry data key marking an HTML-like label {@code <...>}; the value keeps its outer angle brackets. */
    static final String DATA_HTML = "dot.html";

    /** Separator between a port and its compass point, both in DOT syntax and in the CJ port id. */
    static final String COMPASS_SEP = ":";

    private static final Pattern HTML_PORT = Pattern.compile("(?i)\\bport\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)')");

    private DotPorts() {}

    static boolean isCompassPoint(@Nullable String s) {
        return s != null && COMPASS_POINTS.contains(s);
    }

    static boolean isRecordShape(@Nullable String shape) {
        return "record".equals(shape) || "Mrecord".equals(shape);
    }

    /**
     * @param label    the label as written in DOT (quotes removed, backslash escapes kept)
     * @param html     true if the label is an HTML-like string {@code <...>}
     * @param shape    the node's effective shape
     * @return the port names the node declares, in document order
     */
    static Set<String> declaredPorts(@Nullable String label, boolean html, @Nullable String shape) {
        if (label == null) return Set.of();
        if (html) return htmlPorts(label);
        if (isRecordShape(shape)) return recordPorts(label);
        return Set.of();
    }

    /** Ports of an HTML label: every {@code PORT="..."} attribute (attribute names are case-insensitive). */
    static Set<String> htmlPorts(String html) {
        Set<String> ports = new LinkedHashSet<>();
        Matcher m = HTML_PORT.matcher(html);
        while (m.find()) {
            String p = m.group(1) != null ? m.group(1) : m.group(2);
            if (!p.isEmpty()) ports.add(p);
        }
        return ports;
    }

    /**
     * Ports of a record label: the text of every {@code <...>} field marker, trimmed. A backslash escapes the next
     * character, so {@code \<x\>} is literal text, not a port.
     */
    static Set<String> recordPorts(String label) {
        Set<String> ports = new LinkedHashSet<>();
        StringBuilder port = null;
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if (c == '\\' && i + 1 < label.length()) {
                i++;
                if (port != null) port.append(label.charAt(i));
                continue;
            }
            if (port == null) {
                if (c == '<') port = new StringBuilder();
            } else if (c == '>') {
                String p = port.toString().trim();
                if (!p.isEmpty()) ports.add(p);
                port = null;
            } else {
                port.append(c);
            }
        }
        return ports;
    }

    /** The CJ port id for compass point {@code compass} on port {@code port}, e.g. {@code f0:e}. */
    static String compassPortId(@Nullable String port, String compass) {
        return port == null ? compass : port + COMPASS_SEP + compass;
    }
}
