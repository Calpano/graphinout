package com.calpano.graphinout.foundation.xml;

public interface XML {

    /**
     * XML Schema Instance "xmlns:xsi" is <code>http://www.w3.org/2001/XMLSchema-instance</code>.
     */
    @SuppressWarnings("JavadocLinkAsPlainText") String XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    /** Attribute key 'xmlns' */
    String ATT_XMLNS = "xmlns";
    /** Attribute key 'xmlns:xsi' */
    String ATT_XMLNS_XSI = "xmlns:xsi";
    /** Attribute key 'xsi:schemaLocation' */
    String ATT_XSI_SCHEMA_LOCATION = "xsi:schemaLocation";

}
