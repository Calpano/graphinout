package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.GioGraphInOutConstants;
import com.calpano.graphinout.base.GioGraphML;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import com.calpano.graphinout.base.graphml.GraphMLService;

/**
 * @author rbaba
 */
@Slf4j
public final class GioGraphMLServiceFactory {

    private final Map<String, GraphMLService> graphMLServiceStorage = new TreeMap<>();
    private static final GioGraphMLServiceFactory graphMLServiceFactory = new GioGraphMLServiceFactory();

    private GioGraphMLServiceFactory() {
        loadService();
    }

    public static GraphMLService<GioGraphML> instance(String xmlTypeID) throws GioException {

        return graphMLServiceFactory.graphMLServiceStorage.get(xmlTypeID);
    }

    public static GraphMLService<GioGraphML> newInstance(String xmlTypeID) throws GioException {
        //TODO Validate
        //TODO Create and add new Instance to graphMLServiceStorage

        return graphMLServiceFactory.graphMLServiceStorage.get(xmlTypeID);
    }

    public void reloadService() {
        graphMLServiceStorage.clear();
        loadService();
    }

    private void loadService() {
        ServiceLoader<GraphMLService> serviceLoader = ServiceLoader.load(GraphMLService.class);
        log.info(GioGraphInOutConstants.START_LOG);
        log.info("Start to load  GraphMLService.");
        for (GraphMLService xmlService : serviceLoader) {
            log.info("found a GraphMLService  '" + xmlService.getId() + "' !");
            graphMLServiceStorage.put(xmlService.getId(), xmlService);
        }
        log.info("End Load GraphMLService.");
        log.info(GioGraphInOutConstants.END_LOG);

        log.info(GioGraphInOutConstants.START_LOG);
        log.info("Loaded GraphMLService Count is {}", graphMLServiceStorage.size());
        log.info(GioGraphInOutConstants.END_LOG);

    }

}
