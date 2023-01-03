package com.calpano.graphinout.graph.converter;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graph.GioGraphML;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import com.calpano.graphinout.xml.XMLService;

/**
 *
 * @author rbaba
 */
@Slf4j
public final class GioXMLServiceFactory {
    
    private final Map<String, XMLService> xMLServiceStorage = new TreeMap<>();
    private static final GioXMLServiceFactory gioXMLServiceFactory = new GioXMLServiceFactory();
    
    private GioXMLServiceFactory() {        
        loadService();
    }
    
    public static XMLService<GioGraphML> instance(String xmlTypeID) throws GioException {
        
        return gioXMLServiceFactory.xMLServiceStorage.get(xmlTypeID);
    }
    
    public static XMLService<GioGraphML> newInstance(String xmlTypeID) throws GioException {
        //TODO Validate
        //TODO Create and add new Instance to xMLServiceStorage

        return gioXMLServiceFactory.xMLServiceStorage.get(xmlTypeID);
    }
    
    public void reloadService() {
        xMLServiceStorage.clear();
        loadService();
    }
    
    private void loadService() {
        ServiceLoader<XMLService> serviceLoader = ServiceLoader.load(XMLService.class);
        for (XMLService xmlService : serviceLoader) {
            log.info("found a XML Service  '" + xmlService.getId() + "' !");
            xMLServiceStorage.put(xmlService.getId(), xmlService);
        }
        
        log.info("#######-----------------");
        log.info("Loaded XmlService Count is {}", xMLServiceStorage.size());
        
    }
    
}
