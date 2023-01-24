package com.calpano.graphinout.engine;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.exception.GioException;

import java.io.File;

import com.calpano.graphinout.base.graphml.GraphMLService;

/**
 * @author rbaba
 */
public final class GraphMLParser implements Unmarshaller {

    private GraphMLService<GioDocument> graphMLService;


    @Override
    public GioDocument unmarshall(File sourceFile, File outputFile, String inputSourceStructureID) throws GioException {
        graphMLService = GioGraphMLServiceFactory.instance(inputSourceStructureID);
        return graphMLService.getConverter().convert(sourceFile, outputFile,graphMLService);
    }

}
