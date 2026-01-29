package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;

/**
 * Computes the URI based on the ID. If the id is present and contains a colon, it is returned as URI. This includes
 * blank node pseudo-URIs of the form `_:b123`. Other strings are resolved using the current baseUri (from document or
 * graph).
 */
public interface ICjHasUri extends ICjHasId {

    /**
     * Compute this elements URI from this elements ID
     *
     * @return uri of this element
     */
    @NonNull
    String uri();

}
