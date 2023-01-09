//package com.calpano.graphinout.parser;
//
//import com.calpano.graphinout.exception.GioExceptionMessage;
//import com.calpano.graphinout.exception.GioException;
//import com.calpano.graphinout.graph.GioGraphML;
//import jakarta.xml.bind.JAXBContext;
//import jakarta.xml.bind.JAXBException;
//import jakarta.xml.bind.Marshaller;
//import jakarta.xml.bind.Unmarshaller;
//import java.io.File;
//import lombok.extern.slf4j.Slf4j;
//
///**
// *
// * @author rbaba
// */
//@Slf4j
//public class GraphMLParserImpl implements GraphMLParser {
//
//    @Override
//    public GioGraphML unmarshall(String fileName) throws GioException {
//        try {
//
//            //TODO File validation 
//            ///
//            ///
//            if (!new File(fileName).isFile()) {
//                throw new GioException(GioExceptionMessage.XML_FILE_INVALID);
//            }
//            JAXBContext jc = JAXBContext.newInstance(GioGraphML.class.getPackageName());
//
//            Unmarshaller u = jc.createUnmarshaller();
//
//            return (GioGraphML) u.unmarshal(new File(fileName));
//        } catch (JAXBException ex) {
//            log.error(ex.getMessage(), ex);
//
//            throw new GioException(GioExceptionMessage.UNMARSHALLING_ERROR, ex);
//        }
//    }
//
//    @Override
//    public void marshall(String fileName, GioGraphML graphML) throws GioException {
//        try {
//            JAXBContext jc = JAXBContext.newInstance(GioGraphML.class.getPackageName());
//
//            Marshaller u = jc.createMarshaller();
//            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            u.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
//            u.marshal(graphML, new File(fileName));
//        } catch (JAXBException ex) {
//            log.error(ex.getMessage(), ex);
//            throw new GioException(GioExceptionMessage.MARSHALLING_ERROR, ex);
//        }
//
//    }
//
//}
