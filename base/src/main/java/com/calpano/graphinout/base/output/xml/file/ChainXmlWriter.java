package com.calpano.graphinout.base.output.xml.file;

import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.xml.XmlWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@Slf4j
abstract class ChainXmlWriter implements XmlWriter {

    protected final OutputSink outputSink;
    private final Stack<String> elements = new Stack<>();
    protected ChainXmlWriter nextWriter = null;

    protected transient OutputStream out;
    protected transient Writer writer;

    public ChainXmlWriter(OutputSink outputSink) {
        this.outputSink = outputSink;
    }
    @Override
    public void startDocument() throws IOException {
        this.out = outputSink.outputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }
    @Override
    public void characterData(String characterData) throws IOException {
        log.debug("characterData {}.", characterData);
        if(nextWriter !=null){
            nextWriter.characterData(characterData);
        }else{
        writer.append(characterData);
        writer.append(System.lineSeparator());
        writer.flush();
        }
    }

    public void mergeElementFile(String characterData) throws IOException {
        log.debug("merge Element File data [{}].", characterData);
            writer.append(characterData);
            writer.append(System.lineSeparator());
            writer.flush();
    }

    @Override
    public void endDocument() throws IOException {
        log.debug("endDocument.");
        if(nextWriter!=null)
            nextWriter.endDocument();
        else{
            this.writer.flush();
            this.out.flush();
            this.writer.close();
            this.out.close();
        }
    }

    @Override
    public void endElement(String name) throws IOException {
        log.debug("endElement {} ." ,name );

        if(!elementPop().equals(name)){
            //TODO Throw Excption or Report to EndUser
        }
        if(nextWriter !=null) {
            nextWriter.endElement(name);
            if(nextWriter.elementEmpty()) {
                for (String s : nextWriter.readAllElements()) {
                    mergeElementFile(s);
                }
                nextWriter = null;
            }
          //  makeEndElement(name, writer);
        }else {
            makeEndElement(name, writer);
        }
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) throws IOException {
        makeStartElement(name, attributes, writer);
        writer.flush();
    }

    public  void makeStartElement(String name, Map<String, String> attributes, Writer writer) throws IOException {
        writer.append('<');
        writer.append(name);

        for( Map.Entry<String, String> entry : attributes.entrySet()) {
            writer.append(" "+entry.getKey()+"=\""+entry.getValue()+"\"");
        }
        writer.append('>');
        writer.append(System.lineSeparator());
        writer.flush();

    }

    public  void makeEndElement(String name,Writer writer) throws IOException {
        writer.append("</");
        writer.append(name);
        writer.append(">");
        writer.append(System.lineSeparator());
        writer.flush();
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
    public  List<String> readAllElements() throws IOException {
        log.debug("readAllElements from {} .", outputSink.outputInfo());
        return outputSink.readAllData();
    };

}
