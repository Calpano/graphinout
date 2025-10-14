package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement;
import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.foundation.xml.CharactersKind;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;
import com.calpano.graphinout.foundation.xml.XmlNormalizer;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import com.calpano.graphinout.foundation.xml.element.IXmlNode;
import com.calpano.graphinout.foundation.xml.element.XmlElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.calpano.graphinout.base.graphml.GraphmlElements.DATA;
import static com.calpano.graphinout.base.graphml.GraphmlElements.DEFAULT;
import static com.calpano.graphinout.base.graphml.GraphmlElements.DESC;
import static com.calpano.graphinout.base.graphml.GraphmlElements.EDGE;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPHML;
import static com.calpano.graphinout.base.graphml.GraphmlElements.HYPER_EDGE;
import static com.calpano.graphinout.base.graphml.GraphmlElements.KEY;
import static com.calpano.graphinout.base.graphml.GraphmlElements.LOCATOR;
import static com.calpano.graphinout.base.graphml.GraphmlElements.NODE;

/**
 * {@link XmlWriter}that normalizes XML by sorting attributes and stripping comments
 */
public class GraphmlNormalizer {

    static Set<String> skippedKeyIds = Set.of(GraphmlDataElement.EdgeType.attrName, GraphmlDataElement.Label.attrName, GraphmlDataElement.CjJsonData.attrName, GraphmlDataElement.SyntheticNode.attrName);
    private final XmlNormalizer xmlNormalizer;

    public GraphmlNormalizer(String graphmlXmlString) {
        this.xmlNormalizer = new XmlNormalizer(graphmlXmlString);

        normalizeGlobal();

        // == <graphml>
        xmlNormalizer.doc().forEachMatchingElement(elem -> elem.localName().equals(GRAPHML), this::normalizeGraphml);

        // == <graph>
        xmlNormalizer.doc().forEachMatchingElement(elem -> elem.localName().equals(GRAPH), this::normalizeGraph);

        normalizeDatas();
    }

    private void normalizeDatas() {
        // normalize data values
        xmlNormalizer.doc().forEachMatchingElement(elem -> elem.localName().equals(DATA), data -> {
            // normalize weird values like "1." to "1"
            // FIXME this is wrong when a string
            XmlFragmentString xml = data.toXmlFragmentString();
            String rawXml = xml.rawXml();
            if(rawXml.matches("\\d+[.]")) {
                data.setContent(rawXml.substring(0,rawXml.length()-1), CharactersKind.Default);
            }
            // remove xml:space preserve from non-xml values
            if(data.childrenList().isEmpty()) {
                data.removeAttribute(XML.XML_SPACE);
            }
        });

        // remove DATA elements which have the same value as stated by the KEY DEFAULT
        // 1) collected KEY id -> default value as rawXML in a map
        Map<String, String> keyId_defaultValue = new HashMap<>();
        xmlNormalizer.doc().forEachMatchingElement(elem -> elem.localName().equals(KEY), key -> {
            String id = key.attribute("id");
            key.forEachMatchingElement(e -> e.localName().equals(DEFAULT), def -> {
                String prev = keyId_defaultValue.put(id, def.toXmlFragmentString().rawXml());
                assert prev == null : "Duplicate default value for key " + id;
            });
        });

        // 2) remove DATA with matching default value
        xmlNormalizer.removeElementIf(elem -> {
            if (!elem.localName().equals(DATA)) return false;
            String keyId = elem.attribute("key");
            if (keyId == null) return false;
            String defaultValueXml = keyId_defaultValue.get(keyId);
            if (defaultValueXml == null) return false;
            String dataXml = elem.toXmlFragmentString().rawXml();
            return dataXml.equals(defaultValueXml);
        });

        // sort <data> elements within their parent
        {
            xmlNormalizer.doc().forEachMatchingElement(xe -> GraphmlElements.SET_OF_HAS_DATA_ELEMENTS.contains(xe.localName()), //
                    GraphmlNormalizer::sortDataElements);
        }



        // IMPROVE normalize space in data values
    }

    private void normalizeGlobal() {
        // == Every XML Element
        xmlNormalizer.sortAttributesLexicographically();

        // <desc> elements not deemed important for tests
        xmlNormalizer.removeElementIf(elem -> elem.localName().equals(DESC));
    }

    /** only change positions of data elements in this element */
    private static void sortDataElements(XmlElement xmlElement) {
        sortSomeChildElements(xmlElement,
                // 1) extract all <key>
                element -> element.localName().equals(DATA), Comparator.<XmlElement, String> //
                        comparing(k -> k.attributes().get("key")));
    }

    /** only change positions of key elements in this element */
    private static void sortKeyElements(XmlElement xmlElement) {
        sortSomeChildElements(xmlElement,
                // 1) extract all <key>
                element -> element.localName().equals(KEY), Comparator.<XmlElement, String> //
                        comparing(k -> {
                    String for_ = k.attribute("for");
                    if (for_ != null) return for_;
                    return "";
                }) //
                        .thenComparing(k -> {
                            String attrName = k.attribute("attr.name");
                            return attrName != null ? attrName : k.attribute_("id");
                        }));
    }

    private static void sortSomeChildElements(XmlElement xmlElement, Predicate<XmlElement> test, Comparator<XmlElement> xmlElementComparator) {
        List<IXmlNode> list = xmlElement.childrenList();
        // 1) extract some elements
        List<XmlElement> keyList = new ArrayList<>(list.stream().filter(n -> n instanceof XmlElement element && test.test(element)).map(n -> (XmlElement) n).toList());
        keyList.sort(xmlElementComparator);
        // 2) change each original key in the list with a sorted one
        int j = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof XmlElement element && test.test(element)) {
                list.set(i, keyList.get(j++));
            }
        }

    }

    public String resultString() {
        return xmlNormalizer.resultXmlString();
    }

    private void normalizeGraph(XmlElement graph) {
        // remove redundant edgedefault="directed"
        graph.removeAttributeIf((elem, attName, attValue) -> //
                elem.localName().equals(GRAPH) //
                        && attName.equals(IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT) //
                        && attValue.equals(IGraphmlGraph.EdgeDefault.DEFAULT_EDGE_DEFAULT.graphmlString()));

        // remove parseInfo, i.e. <graph> attributes names starting with "parse."
        graph.removeAttributeIf((elem, attName, attValue) -> attName.startsWith("parse."));

        // <edge> set redundant directed=true/false from containing graph
        String attGraphEdgeDefault = graph.attribute(IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT);
        boolean edgeDirected = IGraphmlGraph.EdgeDefault.isDirected(attGraphEdgeDefault);
        graph.forEachMatchingElement(e -> e.localName().equals(EDGE), edge->normalizeEdge(edgeDirected,edge));
        // then remove the now redundant edgedefault from parent graph
        graph.attributes().remove(IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT);

        // sort rules: desc,data,node,edge,hyperedge,locator
        graph.sortChildElementsBySortKey(childElement -> switch (childElement.localName()) {
            case DESC -> "0_desc";
            case DATA -> "1_data";
            case NODE -> "2_node" + childElement.attribute("id");
            case EDGE -> "3_edge" + childElement.attribute("source") +"-"+ childElement.attribute("target");
            case HYPER_EDGE -> "4_hyperedge" + childElement.toXmlFragmentString().rawXml();
            case LOCATOR -> "5_locator";
            default -> throw new IllegalArgumentException("Cannot sort " + childElement);
        });
    }

    private void normalizeEdge(boolean edgeDirectedOfGraph, XmlElement edge) {
        edge.addIfNotPresent("directed", "" + edgeDirectedOfGraph);

        // sort source/target nodes in undirected edge (must happen after we added 'edgedirected')
        boolean edgeIsDirected = IGraphmlGraph.EdgeDefault.isDirected( edge.attribute_("directed"));
        if (!edgeIsDirected) {
            String source = edge.attribute_("source");
            String target = edge.attribute_("target");
            if (source.compareTo(target) > 0) {
                edge.attributeUpdate("source", s -> target);
                edge.attributeUpdate("target", t -> source);
            }
        }
    }

    private void normalizeGraphml(XmlElement graphml) {
        // needs to be done before we try to fix the XSI locations
        xmlNormalizer.normalizeMultipleSpacesInXsiAttributeValue();

        xmlNormalizer.removeIgnorableWhitespace(elem -> //
                GraphmlElements.SET_OF_CONTENT_ELEMENTS.contains(elem.localName()));

        // remove <key> elements inserted by CJ-Graphml-mapping
        xmlNormalizer.removeElementIf(elem -> {
            if (!elem.localName().equals(KEY)) return false;
            String id = elem.attribute("id");
            return id != null && skippedKeyIds.contains(id);
        });
        xmlNormalizer.removeAttributeIf((elem, attName, attValue) -> elem.localName().equals(KEY) && //
                // remove redundant default <key for="all"/>
                ((attName.equals("for") && attValue.equals("all")) ||
                        // remove redundant default <key attr.type="string"/>
                        (attName.equals("attr.type") && attValue.equals("string")) ||
                        // remove redundant default <key attr.name= if equals the ID attr value/>
                        (attName.equals("attr.name") && attValue.equals(elem.attribute("id")))));

        // sort /graphml/key elements by 'for' and then 'id' attribute
        {
            XmlElement rootElement = xmlNormalizer.doc().rootElement();
            sortKeyElements(rootElement);
        }

        // sort rules: desc,key,data,graph
        graphml.sortChildElementsBySortKey(childElement -> switch (childElement.localName()) {
            case DESC -> "0_desc";
            case KEY -> "1_key";
            case DATA -> "2_data";
            case GRAPH -> "3_graph";
            default -> throw new IllegalArgumentException("Cannot sort " + childElement);
        });

        graphml.attributeUpdate(XML.ATT_XMLNS, ns -> switch (ns) {
            // set default namespace to GraphMl if none is defined
            case null -> Graphml.XMLNS_GRAPHML_1_x;
            // auto-fix YWorks 1.0 namespace to GraphML 1.1
            case Graphml.XMLNS_YWORKS_1_0 -> Graphml.XMLNS_GRAPHML_1_x;
            default -> ns;
        });

        // set builtin GraphML namespace, schema location, if not yet defined
        graphml.attributes().putIfAbsent(XML.ATT_XMLNS_XSI, XML.XMLNS_XSI);

        graphml.attributeUpdate(XML.ATT_XSI_SCHEMA_LOCATION, loc -> switch (loc) {
            // if missing, set schema location to GraphML 1.1 // http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd
            case null -> Graphml.XSI_SCHEMA_LOCATION_1_1;
            // auto-upgrade GraphML 1.0 to 1.1
            case Graphml.XSI_SCHEMA_LOCATION_1_0 -> Graphml.XSI_SCHEMA_LOCATION_1_1;
            // auto-fix YWorks 1.x schema location to Graphml 1.1
            case Graphml.XSI_SCHEMA_LOCATION_YWORKS_1_0 -> Graphml.XSI_SCHEMA_LOCATION_1_1;
            case Graphml.XSI_SCHEMA_LOCATION_YWORKS_1_0b -> Graphml.XSI_SCHEMA_LOCATION_1_1;
            case Graphml.XSI_SCHEMA_LOCATION_YWORKS_1_1 -> Graphml.XSI_SCHEMA_LOCATION_1_1;
            default -> loc;
        });

    }

}
