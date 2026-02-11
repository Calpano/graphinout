package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;

/**
 * Computes the URI based on the ID. If the id is present and contains a colon, the prefix is looked up in the
 * document {@code @context}. Blank node pseudo-URIs of the form {@code _:b123} are returned as-is.
 * IDs without a colon are resolved using {@code @vocab} from the document {@code @context}.
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
