package com.graphinout.foundation.json.writer;

/**
 * wor around cyclic interface hierarchy issues
 */
public interface IHasJsonValueWriter {

    JsonValueWriter jsonValueWriter();

}
