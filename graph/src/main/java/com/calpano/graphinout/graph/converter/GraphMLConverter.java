package com.calpano.graphinout.graph.converter;

import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.exception.GioException;
import java.io.File;
import com.calpano.graphinout.xml.XMLService;

/**
 *
 * @author rbaba
 */
public final class GraphMLConverter {

    private XMLService<GioGraphML> xMLType;

    public GioGraphML convert(File XMlFile, String xsdId) throws GioException {
        xMLType = GioXMLServiceFactory.instance(xsdId);
        return xMLType.getConverter().convert(XMlFile, xMLType);

    }

}
