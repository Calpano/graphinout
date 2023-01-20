package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.GioGraphML;

/**
 * @author rbaba
 */
@FunctionalInterface
public interface Marshaller {

    void marshall(String fileName, GioGraphML graphML) throws GioException;
}
