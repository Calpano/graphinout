package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.converter.standard.xml.StandardGraphML;
import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.graphml.GraphMLConverter;
import com.calpano.graphinout.graphml.GraphMLFileValidator;
import com.calpano.graphinout.graphml.GraphMLService;
import com.calpano.graphinout.graphml.GraphMLValidator;
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

import com.calpano.graphinout.graphml.GraphMLValueMapper;
import com.calpano.graphinout.graphml.InputSourceStructure;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author rbaba
 */
public class GioStandardGMLService implements GraphMLService<StandardGraphML> {

    private static final String ID = "graphml-xsd";

    private final GraphMLValueMapper graphMLValueMapper;
    private final InputSourceStructure<File, Void> inputSourceStructure;
    private final GraphMLFileValidator fileValidator;
    private final GraphMLConverter<StandardGraphML> graphMLConverter;

    public GioStandardGMLService() {
        this.inputSourceStructure = (inputStrucure) -> {
            return Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic", "graphml.xsd").toFile();
        };// () -> Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graphml.xsd";
        this.fileValidator = (v, s) -> {
            try {
                initValidator(s).validate(new StreamSource(v));
            } catch (SAXException ex) {
                throw new GioException(SGMLExceptionMessage.SGML_FILE_INCOMPATIBLE, ex);
            } catch (IOException ex) {
                throw new GioException(SGMLExceptionMessage.XML_FILE_ERROR, ex);
            }
        };

        this.graphMLValueMapper = new SGMLValueMapper();
        this.graphMLConverter = new SGMLConverter();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public GraphMLValueMapper getXMlValueMapper() {
        return graphMLValueMapper;
    }

    private Validator initValidator(InputSourceStructure<File, Void> inputfile) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(inputfile.structure(null));
        Schema schema = factory.newSchema(schemaFile);
        return schema.newValidator();
    }

    @Override
    public InputSourceStructure getInputSourceStructure() {
        return inputSourceStructure;
    }

    @Override
    public List<GraphMLValidator> getValueValidators() {
        return new ArrayList<>();
    }

    @Override
    public GraphMLConverter<StandardGraphML> getConverter() {
        return graphMLConverter;
    }

    @Override
    public GraphMLFileValidator getXMLFileValidator() {
        return fileValidator;
    }

}
