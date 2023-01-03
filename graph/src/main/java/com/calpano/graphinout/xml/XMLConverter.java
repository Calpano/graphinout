package com.calpano.graphinout.xml;

import com.calpano.graphinout.exception.GioException;
import java.io.File;

/**
 *
 * @author rbaba
 * @param <T>
 */
@FunctionalInterface
public interface XMLConverter<T> {

    T convert(File xmlFile, XMLService xmlType) throws GioException;

}
