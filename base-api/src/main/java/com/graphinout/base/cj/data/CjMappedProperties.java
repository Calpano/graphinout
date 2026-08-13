package com.graphinout.base.cj.data;

/**
 * Known JSON property key mappings
 */
public class CjMappedProperties {

    /** To be used as a JSON object property key */
    public static final String XML_ATTRIBUTES = "graphml:xmlAttributes";

    /**
     * To be used as an XML attribute name.
     * For storing CJ data in places where the XML format does not allow it.
     */
    public static final String CJ_DATA = "cjData";

    /**
     * To be used as an XML attribute name.
     * For storing CJ endpoint type in XML attribute.
     */
    public static final String CJ_ENDPOINT_TYPE = "cjEndpointType";

}
