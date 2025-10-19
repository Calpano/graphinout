package com.graphinout.base.cj.element;

import java.util.stream.Stream;

/**
 * Represents a CJ array value in the in-memory CJ model. It provides streaming access to its child {@link ICjElement}s.
 */
public interface ICjArray {

    Stream<ICjElement> stream();

}
