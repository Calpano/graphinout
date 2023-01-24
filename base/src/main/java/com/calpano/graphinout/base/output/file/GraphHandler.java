package com.calpano.graphinout.base.output.file;

import com.calpano.graphinout.base.*;
import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.exception.GioExceptionMessage;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioGraphInOutConstants;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.util.GIOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

@Slf4j
 class GraphHandler extends ChainOutputHandler {


    Path outputFilepath;


    FileWriter graphFileWriterTmp;
    BufferedWriter graphBufferedWriterTmp;




    private ElementHandler nodeHandler;
    private ElementHandler edgeHandler;


    @Override
    public void initialize(File file) throws GioException {
        super.initialize(file);

        Path nodeFile = new File(file.getAbsolutePath() + "_" + new Date().getTime() + ".node.tmp").toPath();
        Path edgeFile = new File(file.getAbsolutePath() + "_" + new Date().getTime() + ".edge.tmp").toPath();


            nodeHandler = new ElementHandler();
            nodeHandler.initialize(nodeFile.toFile());
            edgeHandler = new ElementHandler();
            edgeHandler.initialize(edgeFile.toFile());



    }

    @Override
    public void startElement(String name, LinkedHashMap<String, String> attributes) throws GioException {
        log.debug("startElement {} {}.", name, attributes);

        if (nextOutputHandler != null) {
            nextOutputHandler.startElement(name, attributes);
        }
//        else if (GioGraphInOutConstants.GRAPH_ELEMENT_NAME.equals(name) && GioGraphInOutConstants.GRAPH_ELEMENT_NAME.equals(elementPeek())) {
//            nextOutputHandler = new GraphHandler();
//            File tmpGraph = new File(outputFilepath + "_" + new Date().getTime() + ".graph.tmp");
//            nextOutputHandler.initialize(tmpGraph);
//            nextOutputHandler.startElement(name, attributes);
//        }
        else if (GioGraphInOutConstants.NODE_ELEMENT_NAME.equals(name)) {
            nextOutputHandler = nodeHandler;
            nextOutputHandler.startElement(name, attributes);
        } else if (
                GioGraphInOutConstants.EDGE_ELEMENT_NAME.equals(name) ||
                        GioGraphInOutConstants.HYPER_EDGE_ELEMENT_NAME.equals(name)) {
            nextOutputHandler = edgeHandler;
            nextOutputHandler.startElement(name, attributes);
        } else
            this.writeToFile(GIOUtil.makeStartElement(name, attributes));
        elementPush(name);
    }


    @Override
    public void outputFinalize() throws GioException {
        log.debug("outputFinalize.");
        nodeHandler.outputFinalize();
        edgeHandler.outputFinalize();
        if (!elementEmpty()) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }

    }


    @Override
    public void endElement(String name) throws GioException {
        log.debug("endElement {}.", name);
        if(!elementPop().equals(name)){
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }
        if(nextOutputHandler!=null) {
            nextOutputHandler.endElement(name);
            if(nextOutputHandler.elementEmpty()) {
                nextOutputHandler = null;
            }
        }else if(elementEmpty()) {
            List<String> allElements = new ArrayList<>();
            allElements.addAll(nodeHandler.readAllElements());
            allElements.addAll(edgeHandler.readAllElements());
            allElements.forEach( s -> {
                try {
                    super.writeToFile(s);
                } catch (GioException e) {
                    throw new RuntimeException(e);
                }
            });
            writeToFile(GIOUtil.makeEndElement(name));
        }else {
            writeToFile(GIOUtil.makeEndElement(name));
        }

    }


    @Override
    public void startGraphMl(GioDocument gioGraphML) throws IOException {

    }

    @Override
    public void startKey(GioKey gioKey) throws IOException {

    }

    @Override
    public void end(GioKey gioKey) throws IOException {

    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {

    }

    @Override
    public void writeData(XMLValue value) throws IOException {

    }

    @Override
    public void startNode(GioNode node) throws IOException {

    }

    @Override
    public void endNode(GioNode node) throws IOException {

    }

    @Override
    public void startEdge(GioEdge edge) throws IOException {

    }

    @Override
    public void endEdge(GioEdge gioHyperEdge) throws IOException {

    }

    @Override
    public void endGraph(GioGraph gioGraph) throws IOException {

    }

    @Override
    public void endGraphMl(GioDocument gioGraphML) throws IOException {

    }
}
