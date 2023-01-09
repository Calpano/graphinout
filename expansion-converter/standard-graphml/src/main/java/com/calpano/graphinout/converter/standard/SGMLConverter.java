package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.exception.GioExceptionMessage;
import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.graphml.GraphMLConverter;
import com.calpano.graphinout.graphml.GraphMLService;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

/**
 * @author rbaba
 */
@Slf4j
public class SGMLConverter implements GraphMLConverter<GioGraphML> {

    @Override
    public GioGraphML convert(File xmlFile, GraphMLService xmlType) throws GioException {

        try {
            //TODO File validation
            ///
            ///
            if (!xmlFile.isFile()) {
                throw new GioException(GioExceptionMessage.XML_FILE_INVALID);
            }

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            SGMSAXHandler mSAXHandler = new SGMSAXHandler();
            saxParser.parse(xmlFile, mSAXHandler);
            return mSAXHandler.getGioGraphML();
        } catch (ParserConfigurationException ex) {
            throw new GioException(SGMLExceptionMessage.SGML_FILE_INCOMPATIBLE, ex);
        } catch (SAXException ex) {
            throw new GioException(SGMLExceptionMessage.XML_FILE_ERROR, ex);
        } catch (IOException ex) {
            throw new GioException(SGMLExceptionMessage.SGML_FILE_INCOMPATIBLE, ex);
        }

    }

}
