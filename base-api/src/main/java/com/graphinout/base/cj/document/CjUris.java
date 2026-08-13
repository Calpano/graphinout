package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class CjUris {

    public static final String BLANK_NODE_PSEUDO_SCHEME = "_:";

    /**
     * Expand an ID to a full URI using the {@code @context} namespace map.
     * <p>
     * Rules (per CJ 8.0.0 spec):
     * <ol>
     *   <li>If id starts with {@code _:} → blank node identifier, returned as-is.</li>
     *   <li>If id contains {@code :} → prefix (before first colon) is looked up in context.
     *       If found, prefix is replaced with namespace URI. Otherwise, the id is assumed to be a full URI.</li>
     *   <li>If id has no colon and {@code @vocab} is defined in context → {@code @vocab + id}.</li>
     *   <li>If id has no colon and no {@code @vocab} → no URI; the id is returned as-is.</li>
     * </ol>
     *
     * @param context the {@code @context} namespace map (may be null)
     * @param id      the element id
     * @return the expanded URI, or the id itself if no expansion applies
     */
    public static @NonNull String expandId(@Nullable Map<String, String> context, @NonNull String id) {
        // blank node
        if (id.startsWith(BLANK_NODE_PSEUDO_SCHEME)) return id;

        int colon = id.indexOf(':');
        if (colon >= 0) {
            // prefixed id or full URI
            if (context != null) {
                String prefix = id.substring(0, colon);
                String ns = context.get(prefix);
                if (ns != null) {
                    return ns + id.substring(colon + 1);
                }
            }
            // no matching prefix → assumed full URI
            return id;
        }

        // no colon → check @vocab
        if (context != null) {
            String vocab = context.get(CjConstants.VOCAB);
            if (vocab != null) {
                return vocab + id;
            }
        }
        // no URI
        return id;
    }

    /**
     * Abbreviate a URI back to a prefixed ID using the {@code @context} namespace map.
     *
     * @param context the {@code @context} namespace map (may be null)
     * @param uri     the full URI to abbreviate
     * @return the abbreviated prefixed ID, or the original URI if no abbreviation applies
     */
    public static @NonNull String abbreviateUri(@Nullable Map<String, String> context, @NonNull String uri) {
        if (uri.startsWith(BLANK_NODE_PSEUDO_SCHEME)) return uri;
        if (context == null) return uri;

        // try prefix mappings (longest namespace match wins)
        String bestPrefix = null;
        String bestNs = null;
        for (Map.Entry<String, String> entry : context.entrySet()) {
            String key = entry.getKey();
            if (CjConstants.VOCAB.equals(key)) continue;
            String ns = entry.getValue();
            if (uri.startsWith(ns)) {
                if (bestNs == null || ns.length() > bestNs.length()) {
                    bestPrefix = key;
                    bestNs = ns;
                }
            }
        }
        if (bestNs != null) {
            return bestPrefix + ":" + uri.substring(bestNs.length());
        }

        // try @vocab
        String vocab = context.get(CjConstants.VOCAB);
        if (vocab != null && uri.startsWith(vocab)) {
            return uri.substring(vocab.length());
        }

        return uri;
    }

}
