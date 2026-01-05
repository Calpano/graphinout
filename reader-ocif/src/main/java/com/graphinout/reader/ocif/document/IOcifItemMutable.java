package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.pure.annotations.Since;

/** Abstract base type for identified items in the OCIF document (includes entity properties). */
@Since("OCIF 0.6.1")
public interface IOcifItemMutable extends IOcifEntity {

    IOcifItemMutable id(String id);

}
