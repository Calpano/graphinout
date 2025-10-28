package com.graphinout.base;

import java.util.List;

/**
 * The interface used in Java service lookup
 */
public interface GioService {

    /**
     * Id helps debugging the service loader
     *
     * @return
     */
    String id();

    /**
     * @return all {@link GioReader}, which this service provides
     */
    List<GioReader> readers();

}
