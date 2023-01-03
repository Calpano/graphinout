package com.calpano.graphinout.converter.standard;

import com.calpano.graphinout.converter.standard.xml.StandardGraphML;
import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.exception.GioExceptionMessage;
import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.xml.XMLConverter;
import java.io.File;
import com.calpano.graphinout.xml.XMLService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author rbaba
 */
@Slf4j
public class SGMLConverter implements XMLConverter<StandardGraphML> {

    @Override
    public StandardGraphML convert(File xmlFile, XMLService xmlType) throws GioException {
        try {

            //TODO File validation 
            ///
            ///
            if (!xmlFile.isFile()) {
                throw new GioException(GioExceptionMessage.XML_FILE_INVALID);
            }
            JAXBContext jc = JAXBContext.newInstance(StandardGraphML.class);

            Unmarshaller u = jc.createUnmarshaller();

            return (StandardGraphML) u.unmarshal(xmlFile);
        } catch (JAXBException ex) {
            log.error(ex.getMessage(), ex);

            throw new GioException(GioExceptionMessage.UNMARSHALLING_ERROR, ex);
        }
    }

}
