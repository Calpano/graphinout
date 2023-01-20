package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.GioGraphInOutConstants;
import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.exception.GioExceptionMessage;
import com.calpano.graphinout.base.graphml.OutputHandler;
import com.calpano.graphinout.base.util.GIOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Slf4j
public class DefaultFileOutputHandler implements OutputHandler<File> {


    private Path outputFilepath;

   private  final GraphInternalStorage graphInternalStorage = new GraphInternalStorage(); ;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    private DefaultFileOutputHandler currentDefaultFileOutputHandler;

    private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Stack<DefaultFileOutputHandler> defaultFileOutputHandlers = new Stack<>();

    private final Stack<String> elements = new Stack<>();
    private final boolean append ;

    private DefaultFileOutputHandler currentOutputHandler;
    public DefaultFileOutputHandler(boolean append) {
     this.append = append;
        currentOutputHandler = this;
        defaultFileOutputHandlers.push(currentOutputHandler);
    }

    public DefaultFileOutputHandler() {
        this(false);
    }


    @Override
    public void initialize(File file) throws GioException {
        log.debug("initialize file {}.",file.getName());
        try {
            outputFilepath = file.toPath();
            fileWriter = new FileWriter(file,append);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (FileNotFoundException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        } catch (IOException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }
    }
    public void internalInitialize(File file) throws GioException {
        log.debug("initialize file {}.",file.getName());
        try {
            outputFilepath = file.toPath();
            fileWriter = new FileWriter(file,true);
            bufferedWriter = new BufferedWriter(fileWriter);
            graphInternalStorage.initialize(file);
        } catch (FileNotFoundException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        } catch (IOException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }
    }

    @Override
    public void startElement(String name) throws GioException {
        startElement(name, Collections.emptyMap());
    }
    @Override
    public void startElement(String name, Map<String, String> attributes) throws GioException {
        log.debug("startElement {} with attribute [{}].",name,attributes.toString());
        if (GioGraphInOutConstants.GRAPH_ELEMENT_NAME.equals(name)){
            defaultFileOutputHandlers.push(new DefaultFileOutputHandler(true));
            currentDefaultFileOutputHandler = defaultFileOutputHandlers.peek();
            currentDefaultFileOutputHandler.internalInitialize(new File(outputFilepath +"_"+new Date().getTime()+".graph.tmp"));
            currentDefaultFileOutputHandler.startGraphElement(name,attributes);
        }else  if (GioGraphInOutConstants.NODE_ELEMENT_NAME.equals(name) ||
                GioGraphInOutConstants.EDGE_ELEMENT_NAME.equals(name) ||
                GioGraphInOutConstants.HYPER_EDGE_ELEMENT_NAME.equals(name)) {
                currentDefaultFileOutputHandler.startElement( name,attributes);
        }else {
            writeToFile(GIOUtil.makeStartElement(name,attributes));
        }

        elements.push(name);
    }

    public void startGraphElement(String name, Map<String, String> attributes) throws GioException{
        log.debug("startGraphElement {} {}.",name,attributes.toString());

        writeToFile(GIOUtil.makeStartElement(name,attributes));
        elements.push(name);

    }
    @Override
    public void addValue(String... values) throws GioException {
        log.debug("addValue {}.",values);
       if(graphInternalStorage.elementsClosed()){
           writeToFile(String.join(",",values));
       }else{
           graphInternalStorage.addValue(values);
       }

    }

    @Override
    public void endElement(String name) throws GioException {
        log.debug("endElement {}.",name);
        if(!elements.pop().equals(name)){
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }
        if (GioGraphInOutConstants.GRAPH_ELEMENT_NAME.equals(name)){
            if(currentDefaultFileOutputHandler!=null){
            currentDefaultFileOutputHandler.outputFinalize();
            currentDefaultFileOutputHandler.endElement(name);
            currentDefaultFileOutputHandler.readAllElements().stream().forEach(s -> writeToFile(s));
            currentDefaultFileOutputHandler = defaultFileOutputHandlers.pop();
            }else{
               writeToFile(GIOUtil.makeEndElement(name));
            }
        }else  if (GioGraphInOutConstants.NODE_ELEMENT_NAME.equals(name) ||
                GioGraphInOutConstants.EDGE_ELEMENT_NAME.equals(name) ||
                GioGraphInOutConstants.HYPER_EDGE_ELEMENT_NAME.equals(name)) {
            graphInternalStorage.endElement(name);
        }else {
            writeToFile(GIOUtil.makeEndElement(name));
        }


    }

    @Override
    public void outputFinalize() throws GioException {
        log.debug("outputFinalize {}.", outputFilepath.getFileName());
        try {
            bufferedWriter.flush();

        } catch (IOException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }

        graphInternalStorage.outputFinalize();
        graphInternalStorage.readAllElements().forEach(s -> writeToFile(s));


//        defaultFileOutputHandlers.stream().forEach(c -> {
//            try {
//                c.outputFinalize();
//                c.readAllElements().;
//            } catch (GioException e) {
//                throw new RuntimeException( new GioException(GioExceptionMessage.temporary_exemption));
//            }
//        });


    }

    public List<String> readAllElements() throws GioException{

        log.debug("readAllElements from {} .",outputFilepath.getFileName() );


        try {
            return   Files.readAllLines(outputFilepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    private void writeToFile(String value) {
        log.debug("writeToFile  {} .",value);
        try {
            bufferedWriter.write(value);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }


     class GraphInternalStorage implements OutputHandler<File> {
        Path nodeFile;
        Path edgeFile;

        FileWriter nodeFileWriterTmp;
        BufferedWriter nodeBufferedWriterTmp;

        FileWriter edgeFileWriterTmp;
        BufferedWriter edgeBufferedWriterTmp;
        private String currentElement;

        private Stack<String> elements =  new Stack<>();

        @Override
        public void initialize(File masterFile) throws GioException {
            log.debug("initialize  node and edge for {} .",masterFile.getName());
            nodeFile = new File(masterFile.getAbsolutePath() +"_"+ new Date().getTime() + ".node.tmp").toPath();
            edgeFile = new File(masterFile.getAbsolutePath() +"_" + new Date().getTime() + ".edge.tmp").toPath();
            try {
                nodeFileWriterTmp = new FileWriter(nodeFile.toFile());
                nodeBufferedWriterTmp = new BufferedWriter(nodeFileWriterTmp);
                edgeFileWriterTmp = new FileWriter(edgeFile.toFile());
                edgeBufferedWriterTmp = new BufferedWriter(edgeFileWriterTmp);
            } catch (FileNotFoundException e) {
                throw new GioException(GioExceptionMessage.temporary_exemption);
            } catch (IOException e) {
                throw new GioException(GioExceptionMessage.temporary_exemption);
            }


        }
        @Override
        public void startElement(String name) throws GioException {
            this.startElement(name, Collections.emptyMap());
        }

        @Override
        public void startElement(String name, Map<String, String> attributes) throws GioException {
            log.debug("startElement {} {}.",name,attributes);
            if (GioGraphInOutConstants.NODE_ELEMENT_NAME.equals(name) ||
                    GioGraphInOutConstants.EDGE_ELEMENT_NAME.equals(name) ||
                    GioGraphInOutConstants.HYPER_EDGE_ELEMENT_NAME.equals(name)) {
                currentElement = name;
            }
            elements.push(name);
            this.writeToFile(GIOUtil.makeStartElement(name, attributes));

        }

        @Override
        public void addValue(String... values) throws GioException {
            this.writeToFile(String.join(",",values));
        }

        @Override
        public void endElement(String name) throws GioException {
            log.debug("endElement {} .",name);
            if(elements.isEmpty() || !elements.peek().equals(name)){
                throw new GioException(GioExceptionMessage.temporary_exemption);
            }

            this.writeToFile(GIOUtil.makeEndElement(name));

            elements.pop();
        }

        @Override
        public void outputFinalize() throws GioException {
            log.debug("outputFinalize.");
            try {
                nodeBufferedWriterTmp.flush();
            } catch (IOException e) {
                throw new GioException(GioExceptionMessage.temporary_exemption);
            }finally {
                try {
                    nodeBufferedWriterTmp.close();
                } catch (IOException e) {
                    throw new GioException(GioExceptionMessage.temporary_exemption);
                }
        }
            try {
                edgeBufferedWriterTmp.flush();
            } catch (IOException e) {
                throw new GioException(GioExceptionMessage.temporary_exemption);
            }finally {
                try {
                    edgeBufferedWriterTmp.close();
                } catch (IOException e) {
                    throw new GioException(GioExceptionMessage.temporary_exemption);
                }

            }

            if(!elements.isEmpty()){
                throw new GioException(GioExceptionMessage.temporary_exemption);
            }


        }

        public List<String> readAllElements() throws GioException{

            log.debug("readAllElements from {} and {} .",nodeFile.getFileName(),edgeFile.getFileName() );
             List<String> allElements =  new ArrayList<>();

            try {
                allElements.addAll(Files.readAllLines(nodeFile));
                allElements.addAll(Files.readAllLines(edgeFile));
                return allElements;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private void writeToFile(String value) {
            log.debug("writeToFile {}  for {}.",value,currentElement);
            switch (currentElement) {
                case GioGraphInOutConstants.EDGE_ELEMENT_NAME:
                case GioGraphInOutConstants.HYPER_EDGE_ELEMENT_NAME:
                    try {
                        edgeBufferedWriterTmp.newLine();
                        edgeBufferedWriterTmp.flush();
                        edgeBufferedWriterTmp.write(value);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case GioGraphInOutConstants.NODE_ELEMENT_NAME:
                    try {
                    nodeBufferedWriterTmp.write(value);
                    nodeBufferedWriterTmp.newLine();
                    edgeBufferedWriterTmp.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;

            }


        }

        public boolean elementsClosed(){
           return elements.isEmpty();
        }
    }
}
