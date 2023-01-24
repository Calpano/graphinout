package com.calpano.graphinout.base.output.file;

import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.exception.GioExceptionMessage;
import com.calpano.graphinout.base.output.OutputHandler;
import com.calpano.graphinout.base.util.GIOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

@Slf4j
abstract class ChainOutputHandler implements OutputHandler<File> {

    protected Path outputFilepath;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    protected ChainOutputHandler nextOutputHandler= null;
    private final Stack<String> elements = new Stack<>();





    @Override
    public void initialize(File file) throws GioException {
        log.debug("initialize file {}.", file.getName());
        try {
            outputFilepath = file.toPath();
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (FileNotFoundException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        } catch (IOException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }
    }

    public void elementPush(String name){
        elements.push(name);
    }

    public String elementPeek(){
        if(elements.isEmpty())
            return null;
       return elements.peek();
    }

    public String elementPop(){
        return elements.pop();
    }

    public boolean elementEmpty(){
        return elements.isEmpty();
    }



    void writeToFile(String value) throws GioException {
        log.debug("writeToFile  {} .", value);
        try {
            bufferedWriter.write(value);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void startElement(String name) throws GioException {
        this.startElement(name, new LinkedHashMap<>());
    }

    @Override
    public void addValue(String... values) throws GioException {
        log.debug("addValue {}.", values);
        if(nextOutputHandler!=null){
            nextOutputHandler.addValue(values);
        }else
            writeToFile(String.join(",", values));

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
                nextOutputHandler.readAllElements().forEach(s -> {
                            try {
                                writeToFile(s);
                } catch (GioException e) {
                    throw new RuntimeException(e);
                }
            });
                nextOutputHandler = null;
            }
        }else if(elementEmpty()) {
//                        readAllElements().forEach(s -> {
//                            try {
//                                writeToFile(s);
//                } catch (GioException e) {
//                    throw new RuntimeException(e);
//                }
//            });
            writeToFile(GIOUtil.makeEndElement(name));
        }else {
            writeToFile(GIOUtil.makeEndElement(name));
        }

    }


    @Override
    public void outputFinalize() throws GioException {
        log.debug("outputFinalize {}.", outputFilepath.getFileName());
        try {
            if(nextOutputHandler!=null){
                nextOutputHandler.outputFinalize();
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }


    }

    List<String> readAllElements() throws GioException {
        log.debug("readAllElements from {} .", outputFilepath.getFileName());
        try {
            return Files.readAllLines(outputFilepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
