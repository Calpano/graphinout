package com.calpano.graphinout.foundation.json.stream;

/**
 * wor around cyclic interface hierarchy issues
 */
public interface HasJsonValueWriter {

    JsonValueWriter jsonValueWriter();

}
