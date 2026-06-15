package com.graphinout.base.cj.writer;

/**
 * Mixin for components that expose the {@link com.graphinout.base.cj.writer.ICjWriter} they emit CJ events to.
 */
public interface IHasCjWriter {

    ICjWriter cjWriter();

}
