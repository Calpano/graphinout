package com.calpano.graphinout.base.output.file;

import com.calpano.graphinout.base.GioGraphInOutConstants;
import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.util.GIOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Slf4j
 class ElementHandler extends ChainOutputHandler {

    private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;



    @Override
    public void startElement(String name, LinkedHashMap<String, String> attributes) throws GioException {
        log.debug("startElement {} with attribute [{}].", name, attributes.toString());
        if(nextOutputHandler!=null){
            nextOutputHandler.startElement(name,attributes);
        } else if (GioGraphInOutConstants.GRAPH_ELEMENT_NAME.equals(name)) {
            nextOutputHandler =  new GraphHandler();
            File tmpGraph = new File(outputFilepath +"_"+new Date().getTime()+".graph.tmp");
            nextOutputHandler.initialize(tmpGraph);
            nextOutputHandler.startElement(name,attributes);
        }else {
            writeToFile(GIOUtil.makeStartElement(name, attributes));
        }
        elementPush(name);
    }
    @Override
     List<String> readAllElements() throws GioException {
        log.debug("readAllElements from {} .", outputFilepath.getFileName());
        try {
            return Files.readAllLines(outputFilepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
