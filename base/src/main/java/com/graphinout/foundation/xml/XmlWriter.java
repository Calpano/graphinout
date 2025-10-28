package com.graphinout.foundation.xml;

import com.graphinout.foundation.input.ContentErrorAware;
import com.graphinout.foundation.input.IHandleContentErrors;
import com.graphinout.foundation.input.LocationAware;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * XML writer without namespace support, but we do allow colons in element names
 */
public interface XmlWriter extends XmlCharacterWriter, IHandleContentErrors {

    void documentEnd() throws IOException;

    void documentStart() throws IOException;

    /**
     *
     * @param uri       '{@code http://example.org/schema/1.0}' -- the current namespace, often '' empty string.
     * @param localName 'myElement' - excludes all namespace prefixes
     * @param qName     'x:myElement' verbatim as written in XML
     */
    void elementEnd(String uri, String localName, String qName) throws IOException;

    /**
     * This allows creating malformed XML but makes understanding errors easier
     *
     * @param xmlName redundantly, the ending elements name.
     */
    default void elementEnd(IXmlName xmlName) throws IOException {
        elementEnd(xmlName.uri(), xmlName.localName(), xmlName.qName());
    }

    default void elementStart(IXmlName xmlName) throws IOException {
        elementStart(xmlName.uri(), xmlName.localName(), xmlName.qName(), Collections.emptyMap());
    }

    default void elementStart(IXmlName xmlName, Map<String, String> attributes) throws IOException {
        elementStart(xmlName.uri(), xmlName.localName(), xmlName.qName(), attributes);
    }

    default void elementStart(String uri, String localName, String qName) throws IOException {
        elementStart(uri, localName, qName, Collections.emptyMap());
    }

    /**
     *
     * @param uri        '{@code http://example.org/schema/1.0}' -- the current namespace, often '' empty string.
     * @param localName  'myElement' - excludes all namespace prefixes
     * @param qName      'x:myElement' verbatim as written in XML
     * @param attributes "The order of attributes in the list is unspecified, and will vary from implementation to
     *                   implementation."
     */
    void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException;

}
