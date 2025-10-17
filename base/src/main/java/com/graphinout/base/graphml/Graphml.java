package com.graphinout.base.graphml;

import com.graphinout.foundation.xml.IXmlName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Relevant schemas:
 * <li><a href="http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd">GraphML 1.1</a></li>
 * <li><a href="http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">GraphML 1.0</a></li>
 * <li><a href="http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd">YWorks GraphML 1.0</a></li>
 * <li><a href="http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">YWorks GraphML 1.1</a></li>
 */
public class Graphml {

    /** TODO unclear role of this spec */
    public static final String XSI_SCHEMA_ATTRIBUTES_10_RC = "http://graphml.graphdrawing.org/xmlns/graphml/graphml-attributes-1.0rc.xsd";

    /** For Graphml 1.0 and 1.1, YWorks Grapml 1.1 */
    public static final String XMLNS_GRAPHML_1_x = "http://graphml.graphdrawing.org/xmlns";
    public static final String XMLNS_YWORKS_1_0 = "http://graphml.graphdrawing.org/xmlns/graphml";
    /** "xsi:schemaLocation" 1.0 */
    public static final String XSI_SCHEMA_LOCATION_1_0 = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    /** "xsi:schemaLocation" 1.1 */
    public static final String XSI_SCHEMA_LOCATION_1_1 = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd";
    static Map<String, Set<String>> parent_child;

    /** http://www.yworks.com/xml/schema/graphml/1.1 */
    public static final String XSI_SCHEMA_LOCATION_YWORKS_1_1 = "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd";
    public static final String XSI_SCHEMA_LOCATION_YWORKS_1_0= "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd";public static final String XSI_SCHEMA_LOCATION_YWORKS_1_0b= "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd";

    static {
        parent_child = new HashMap<>();
        parent_child.computeIfAbsent("graphml", k -> new HashSet<>()).add("graph");
        parent_child.computeIfAbsent("graphml", k -> new HashSet<>()).add("data");
        parent_child.computeIfAbsent("graphml", k -> new HashSet<>()).add("key");
        parent_child.computeIfAbsent("graph", k -> new HashSet<>()).add("data");
        parent_child.computeIfAbsent("graph", k -> new HashSet<>()).add("node");
        parent_child.computeIfAbsent("graph", k -> new HashSet<>()).add("edge");
        parent_child.computeIfAbsent("graph", k -> new HashSet<>()).add("hyperedge");
        parent_child.computeIfAbsent("node", k -> new HashSet<>()).add("data");
        parent_child.computeIfAbsent("node", k -> new HashSet<>()).add("port");
        parent_child.computeIfAbsent("node", k -> new HashSet<>()).add("graph");
        parent_child.computeIfAbsent("port", k -> new HashSet<>()).add("data");
        parent_child.computeIfAbsent("port", k -> new HashSet<>()).add("port");
        parent_child.computeIfAbsent("edge", k -> new HashSet<>()).add("data");
        parent_child.computeIfAbsent("edge", k -> new HashSet<>()).add("graph");
        parent_child.computeIfAbsent("hyperedge", k -> new HashSet<>()).add("data");
        parent_child.computeIfAbsent("hyperedge", k -> new HashSet<>()).add("endpoint");
        parent_child.computeIfAbsent("hyperedge", k -> new HashSet<>()).add("graph");
    }

    public static boolean isXmlChildElementWithIndependentGioWriterCall(String parent, String child) {
        Set<String> set = parent_child.get(parent);
        return set != null && set.contains(child);
    }

    public static IXmlName xmlNameOf(String graphmlLocalName) {
        return IXmlName.of(XMLNS_GRAPHML_1_x, graphmlLocalName, graphmlLocalName);
    }

}
