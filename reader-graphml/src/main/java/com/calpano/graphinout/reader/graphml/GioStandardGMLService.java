//package com.calpano.graphinout.reader.graphml;
//
//import com.calpano.graphinout.base.exception.GioException;
//import com.calpano.graphinout.base.gio.GioDocument;
//import com.calpano.graphinout.base.graphml.*;
//
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.List;
//import javax.xml.XMLConstants;
//import javax.xml.transform.Source;
//import javax.xml.transform.stream.StreamSource;
//import javax.xml.validation.Schema;
//import javax.xml.validation.SchemaFactory;
//import javax.xml.validation.Validator;
//
//import org.xml.sax.SAXException;
//
//import java.io.File;
//import java.util.ArrayList;
//
///**
// * @author rbaba
// */
//public class GioStandardGMLService implements GraphMLService<GioDocument> {
//
//    private static final String ID = "graphml-xsd";
//
//    private final GraphMLValueMapper graphMLValueMapper;
//    private final InputSourceStructure<File, Void> inputSourceStructure;
//    private final GraphMLFileValidator fileValidator;
//    private final GraphMLConverter<GioDocument> graphMLConverter;
//
//    public GioStandardGMLService() {
//        this.inputSourceStructure = (inputStructure) -> {
//            return Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic", "graphml.xsd").toFile();
//        };// () -> Paths.get("src", "test", "resources", "graphin", "graphml", "synthetic").toString() + "/graphml.xsd";
//        this.fileValidator = (v, s) -> {
//            try {
//                initValidator(s).validate(new StreamSource(v));
//            } catch (SAXException ex) {
//                throw new GioException(SGMLExceptionMessage.SGML_FILE_INCOMPATIBLE, ex);
//            } catch (IOException ex) {
//                throw new GioException(SGMLExceptionMessage.XML_FILE_ERROR, ex);
//            }
//        };
//
//        this.graphMLValueMapper = new SGMLValueMapper();
//        this.graphMLConverter = new SGMLConverter();
//    }
//
//    @Override
//    public String getId() {
//        return ID;
//    }
//
//    @Override
//    public GraphMLValueMapper getXMlValueMapper() {
//        return graphMLValueMapper;
//    }
//
//    private Validator initValidator(InputSourceStructure<File, Void> inputFile) throws SAXException {
//        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        Source schemaFile = new StreamSource(inputFile.structure(null));
//        Schema schema = factory.newSchema(schemaFile);
//        return schema.newValidator();
//    }
//
//    @Override
//    public InputSourceStructure getInputSourceStructure() {
//        return inputSourceStructure;
//    }
//
//    @Override
//    public List<GraphMLValidator> getValueValidators() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public GraphMLConverter<GioDocument> getConverter() {
//        return graphMLConverter;
//    }
//
//
//    @Override
//    public GraphMLFileValidator getXMLFileValidator() {
//        return fileValidator;
//    }
//
//}
