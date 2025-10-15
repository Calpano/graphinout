package com.graphinout.base.cj.helper;

import java.util.Map;

/**
 * Represents a label that can be either a simple string or a multi-lingual object.
 */
public interface ICjLabel {

    /**
     * @return the multilingual label as a map of language codes to labels
     * @throws IllegalStateException if this is not a multilingual label
     */
    Map<String, String> asMultiLingual();

    /**
     * @return the simple string label
     * @throws IllegalStateException if this is not a simple label
     */
    String asString();

    /**
     * @return true if this is a multilingual label
     */
    boolean isMultiLingual();

    /**
     * @return true if this is a simple string label
     */
    boolean isSimple();

}
