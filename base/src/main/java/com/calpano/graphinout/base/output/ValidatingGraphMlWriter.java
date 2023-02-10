//package com.calpano.graphinout.base.output;
//
//import com.calpano.graphinout.base.gio.*;
//import com.calpano.graphinout.base.graphml.GraphmlWriter;
//import org.xml.sax.SAXException;
//
//import javax.xml.XMLConstants;
//import javax.xml.transform.*;
//import javax.xml.transform.stream.StreamSource;
//import javax.xml.validation.Schema;
//import javax.xml.validation.SchemaFactory;
//import javax.xml.validation.Validator;
//import java.io.IOException;
//import java.io.StringReader;
//
//public class ValidatingGraphMlWriter implements GraphmlWriter {
//    private GraphmlWriter graphMlWriter;
//
//    public ValidatingGraphMlWriter (GraphmlWriter graphMlWriter) {
//        this.graphMlWriter = graphMlWriter;
//    }
//
//    @Override
//    public void startGraphMl(GioDocument gioGraphML) throws IOException {
//        graphMlWriter.startGraphMl(gioGraphML);
//    }
//
//    @Override
//    public void startKey(GioKey gioKey) throws IOException {
//        graphMlWriter.startKey(gioKey);
//    }
//
//    @Override
//    public void end(GioKey gioKey) throws IOException {
//        graphMlWriter.end(gioKey);
//    }
//
//    @Override
//    public void startGraph(GioGraph gioGraph) throws IOException {
//        graphMlWriter.startGraph(gioGraph);
//    }
//
//    @Override
//    public void startNode(GioNode node) throws IOException {
//        graphMlWriter.startNode(node);
//    }
//
//    @Override
//    public void endNode(GioNode node) throws IOException {
//        graphMlWriter.endNode(node);
//    }
//
//    @Override
//    public void startEdge(GioEdge edge) throws IOException {
//        graphMlWriter.startEdge(edge);
//    }
//
//    @Override
//    public void endEdge(GioEdge gioHyperEdge) throws IOException {
//        graphMlWriter.endEdge(gioHyperEdge);
//    }
//
//    @Override
//    public void endGraph(GioGraph gioGraph) throws IOException {
//        graphMlWriter.endGraph(gioGraph);
//    }
//
//    @Override
//    public void endGraphMl(GioDocument gioGraphML) throws IOException {
//        graphMlWriter.endGraphMl(gioGraphML);
//    }
//
//    // TODO move inside GraphmValidator
//    private void validateGraphMlStructure(String graphMLString) throws IOException {
//        try {
//            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//            Source schemaFile = new StreamSource(new StringReader("<!DOCTYPE graphml SYSTEM \"http://graphml.graphdrawing.org/dtds/1.0/graphml.dtd\">"));
//            Schema schema = factory.newSchema(schemaFile);
//            Validator validator = schema.newValidator();
//            Source source = new StreamSource(new StringReader(graphMLString));
//            validator.validate(source);
//        } catch (SAXException e) {
//            throw new IOException("Validation of GraphML was not successful.", e);
//        }
//    }
//}
