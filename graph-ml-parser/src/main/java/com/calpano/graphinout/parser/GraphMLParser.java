package com.calpano.graphinout.parser;

import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.exception.GioException;
import java.io.File;
import com.calpano.graphinout.graphml.GraphMLService;

/**
 *
 * @author rbaba
 */
public final class GraphMLParser implements Unmarshaller {

    private GraphMLService<GioGraphML> graphMLService;

    

    @Override
    public GioGraphML unmarshall(File sourceFile, String inputSourceStructureID) throws GioException {
        graphMLService = GioGraphMLServiceFactory.instance(inputSourceStructureID);
        return graphMLService.getConverter().convert(sourceFile, graphMLService);
    }

}
