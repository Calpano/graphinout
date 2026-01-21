package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

public class CjUris {

    public static final String BLANK_NODE_PSEUDO_SCHEME = "_:";

    /**
     * Combine baseUri with localName to a full URI. If the baseUri ends with a slash, concatenate. Otherwise, insert a
     * hash mark. This behavior reflects the typical RDF vocabularies.
     * <p>
     * TODO add this to CJ spec
     *
     * @param baseUri   the effective baseUri to use
     * @param localName the local name <em>which may already be a URI</em>. It should not start with slash or hash mark, but an alpha numeric
     * @return a URI composed of baseUri and localName
     */
    public static @NonNull String uri(@Nullable String baseUri, @NonNull String localName) {
        if (localName.contains(":")) return localName;
        String base = nonNullOrDefault(baseUri, "#");
        if (!base.endsWith("/") && !base.endsWith("#")) {
            base += "#";
        }
        return base + localName;
    }

}
