package com.graphinout.foundation.pure.json.writer;

/**
 * wor around cyclic interface hierarchy issues
 */
public interface IHasJsonValueWriter {

    JsonValueWriter jsonValueWriter();

}
