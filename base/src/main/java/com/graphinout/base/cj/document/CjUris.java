package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

public class CjUris {

    public static final String BLANK_NODE_PSEUDO_SCHEME = "_:";
    public static final String BASE_URI_FALLBACK = "";
    public static final String SLASH = "/";
    public static final String HASH = "#";

    /**
     * Combine baseUri with localName to a full URI. This behavior reflects the typical RDF vocabularies.
     *
     * @param baseUri        the effective baseUri to use
     * @param localNameOrUri the local name <em>which may already be a URI</em>. It should not start with a slash or
     *                       hash mark, but an alphanumeric character.
     * @return a URI composed of baseUri and localName
     */
    public static @NonNull String uri(@Nullable String baseUri, @NonNull String localNameOrUri) {
        if (localNameOrUri.contains(":"))
            // looks already like a URI
            return localNameOrUri;
        String base = nonNullOrDefault(baseUri, BASE_URI_FALLBACK);
        if (base.isEmpty()) return localNameOrUri;
        return base + localNameOrUri;
    }

}
