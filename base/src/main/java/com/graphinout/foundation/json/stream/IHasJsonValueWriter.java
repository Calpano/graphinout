package com.graphinout.foundation.json.stream;

/**
 * wor around cyclic interface hierarchy issues
 */
public interface IHasJsonValueWriter {

    JsonValueWriter jsonValueWriter();

}
