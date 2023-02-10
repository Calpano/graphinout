package com.calpano.graphinout.base.output.xml;

import com.calpano.graphinout.base.input.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

public class GraphmlValidator {

    /**
     * @param inputSource
     * @return
     */
    public static boolean isValidGraphml(InputSource inputSource) throws IOException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(new StringReader("<!DOCTYPE graphml SYSTEM \"http://graphml.graphdrawing.org/dtds/1.0rc/graphml.dtd\">"));
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            Source source = new SAXSource(new org.xml.sax.InputSource(inputSource.inputStream()));
            validator.validate(source);
            return true;
        } catch (SAXException e) {
            return false;
        }
    }
}
