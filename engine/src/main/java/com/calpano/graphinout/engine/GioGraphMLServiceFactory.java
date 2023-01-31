package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.graphml.GraphMLService;
import com.calpano.graphinout.base.output.xml.GioGraphInOutXMLConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

/**
 * @author rbaba
 */
@Slf4j
public final class GioGraphMLServiceFactory {

    private static final GioGraphMLServiceFactory graphMLServiceFactory = new GioGraphMLServiceFactory();
    private final Map<String, GraphMLService> graphMLServiceStorage = new TreeMap<>();

    private GioGraphMLServiceFactory() {
        loadService();
    }

    public static GraphMLService<GioDocument> instance(String xmlTypeID) throws GioException {

        return graphMLServiceFactory.graphMLServiceStorage.get(xmlTypeID);
    }

    public static GraphMLService<GioDocument> newInstance(String xmlTypeID) throws GioException {
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
        log.info(GioGraphInOutXMLConstants.START_LOG);
        log.info("Start to load  GraphMLService.");
        for (GraphMLService xmlService : serviceLoader) {
            log.info("found a GraphMLService  '" + xmlService.getId() + "' !");
            graphMLServiceStorage.put(xmlService.getId(), xmlService);
        }
        log.info("End Load GraphMLService.");
        log.info(GioGraphInOutXMLConstants.END_LOG);

        log.info(GioGraphInOutXMLConstants.START_LOG);
        log.info("Loaded GraphMLService Count is {}", graphMLServiceStorage.size());
        log.info(GioGraphInOutXMLConstants.END_LOG);

    }

}
