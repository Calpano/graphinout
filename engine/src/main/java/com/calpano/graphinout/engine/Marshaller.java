package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.gio.GioDocument;

/**
 * @author rbaba
 */
@FunctionalInterface
public interface Marshaller {

    void marshall(String fileName, GioDocument graphML) throws GioException;
}
