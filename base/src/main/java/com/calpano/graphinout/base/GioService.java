package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioReader;

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
