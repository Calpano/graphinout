package com.calpano.graphinout.xml;

import java.util.List;

/**
 *
 * @author rbaba
 * @param <T>
 */
public interface XMLService<T> {

    public String getId();

    public XSD getXsd();

    public XMlValueMapper getXMlValueMapper();

    public XMLFileValidator getXMLFileValidator();

    public List<XMLValidator> getXMLValidators();

    public XMLConverter<T> getConverter();

}
