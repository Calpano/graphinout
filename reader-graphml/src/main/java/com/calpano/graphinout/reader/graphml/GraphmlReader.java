package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.function.Consumer;

public class GraphmlReader implements GioReader {
  private Consumer<ContentError> errorConsumer ;
    @Override
    public void errorHandler(Consumer<ContentError> errorConsumer) {
        GioReader.super.errorHandler(errorConsumer);
        this.errorConsumer= errorConsumer;
    }

    @Override
    public GioFileFormat fileFormat() {
        //TODO @Maxwe need multi types files support for example "xml graphml "
        return new GioFileFormat("xml"/*,"graphml"*/, "XML Graph Format.");
    }

    @Override
    public boolean isValid(SingleInputSource singleInputSource) throws IOException {
        // TODO
        return GioReader.super.isValid(singleInputSource);
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();

        GraphmlSAXHandler saxHandler  =  new GraphmlSAXHandler(writer,this.errorConsumer);
        if(inputSource.isSingle())
        saxParser.parse(((SingleInputSource)inputSource).inputStream(),saxHandler);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }

    }
}
