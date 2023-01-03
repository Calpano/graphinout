package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.converter.standard.xml.StandardGraphML;
import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.xml.XMLConverter;
import com.calpano.graphinout.xml.XMLFileValidator;
import com.calpano.graphinout.xml.XMLValidator;
import com.calpano.graphinout.xml.XMlValueMapper;

import com.calpano.graphinout.xml.XSD;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import com.calpano.graphinout.xml.XMLService;

/**
 *
 * @author rbaba
 */
public class GioStandardGMLService implements XMLService<StandardGraphML> {

    private static final String ID = "graphml-xsd";

    private final XSD myxsd;
    private final XMLFileValidator myXMLFileValidator;
    private final XMlValueMapper myXMlValueMapper;
    private final XMLConverter<StandardGraphML> myXMLConverter;

    public GioStandardGMLService() {
        this.myxsd = () -> Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graphml.xsd";
        this.myXMLFileValidator = (value, Type) -> {
            try {
                initValidator(Type.getFilePath()).validate(new StreamSource(value));
            } catch (SAXException ex) {
                throw new GioException(SGMLExceptionMessage.SGML_FILE_INCOMPATIBLE, ex);
            } catch (IOException ex) {
                throw new GioException(SGMLExceptionMessage.XML_FILE_ERROR, ex);
            }
        };

        this.myXMlValueMapper = new SGMLValueMapper();
        this.myXMLConverter = new SGMLConverter();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public XSD getXsd() {
        return myxsd;
    }

    @Override
    public XMLFileValidator getXMLFileValidator() {
        return myXMLFileValidator;
    }

    ;

    

    @Override
    public List<XMLValidator> getXMLValidators() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public XMlValueMapper getXMlValueMapper() {
        return myXMlValueMapper;
    }

    @Override
    public XMLConverter<StandardGraphML> getConverter() {
        return myXMLConverter;
    }

    private Validator initValidator(String xsdPath) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsdPath);
        Schema schema = factory.newSchema(schemaFile);
        return schema.newValidator();
    }

}
