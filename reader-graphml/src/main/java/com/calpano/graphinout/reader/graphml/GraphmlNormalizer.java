package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement;
import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlNormalizer;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import com.calpano.graphinout.foundation.xml.element.XmlElement;
import com.calpano.graphinout.foundation.xml.element.XmlNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static com.calpano.graphinout.base.graphml.GraphmlElements.DATA;
import static com.calpano.graphinout.base.graphml.GraphmlElements.DESC;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPHML;
import static com.calpano.graphinout.base.graphml.GraphmlElements.KEY;

/**
 * {@link XmlWriter}that normalizes XML by sorting attributes and stripping comments
 */
public class GraphmlNormalizer {

    static Set<String> skippedKeyIds = Set.of(GraphmlDataElement.EdgeType.attrName, GraphmlDataElement.Label.attrName, GraphmlDataElement.CjJsonData.attrName, GraphmlDataElement.SyntheticNode.attrName);
    private final XmlNormalizer xmlNormalizer;

    public GraphmlNormalizer(String graphmlXmlString) {
        this.xmlNormalizer = new XmlNormalizer(graphmlXmlString);

        xmlNormalizer.sortAttributesLexicographically();

        // remove <key> elements inserted by CJ-Graphml-mapping
        xmlNormalizer.removeElementIf(elem -> {
            if (!elem.localName().equals(KEY)) return false;
            String id = elem.attribute("id");
            return id != null && skippedKeyIds.contains(id);
        });

        // <desc> elements not deemed important for tests
        xmlNormalizer.removeElementIf(elem -> elem.localName().equals(DESC));

        // remove redundant default <key for="all"/>
        xmlNormalizer.removeAttributeIf((elem, attName, attValue) -> //
                elem.localName().equals(KEY) && attName.equals("for") && attValue.equals("all"));
        // remove redundant default <key attr.type="string"/>
        xmlNormalizer.removeAttributeIf((elem, attName, attValue) -> //
                elem.localName().equals(KEY) && attName.equals("attr.type") && attValue.equals("string"));
        // remove redundant default <key attr.name= if equals the ID attr value/>
        xmlNormalizer.removeAttributeIf((elem, attName, attValue) -> //
                elem.localName().equals(KEY) && attName.equals("attr.name") && attValue.equals(elem.attribute("id")));

        // remove graph.edgedefault
        xmlNormalizer.removeAttributeIf((elem, attName, attValue) -> //
                elem.localName().equals(GRAPH) && attName.equals(IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT));

        // needs to be done before we try to fix the XSI locations
        xmlNormalizer.normalizeMultipleSpacesInXsiAttributeValue();

        xmlNormalizer.doc().forEachMatchingElement(elem -> elem.localName().equals(GRAPHML), graphml -> {
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
                // if missing, set schema location to GraphML 1.1
                case null -> Graphml.XSI_SCHEMA_LOCATION_1_1;
                // auto-upgrade GraphML 1.0 to 1.1
                case Graphml.XSI_SCHEMA_LOCATION_1_0 -> Graphml.XSI_SCHEMA_LOCATION_1_1;
                // auto-fix YWorks 1.x schema location to Graphml 1.1
                case Graphml.XSI_SCHEMA_LOCATION_YWORKS_1_0 -> Graphml.XSI_SCHEMA_LOCATION_1_1;
                case Graphml.XSI_SCHEMA_LOCATION_YWORKS_1_1 -> Graphml.XSI_SCHEMA_LOCATION_1_1;
                default -> loc;
            });
        });

        xmlNormalizer.doc().forEachMatchingElement(elem -> elem.localName().equals(GRAPH), graph -> {
            // remove redundant edgedefault="directed"
            graph.removeAttributeIf((elem, attName, attValue) -> attName.equals(IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT) && attValue.equals(IGraphmlGraph.EdgeDefault.DEFAULT_EDGE_DEFAULT.graphmlString()));

            // remove parseInfo, i.e. <graph> attributes names starting with "parse."
            graph.removeAttributeIf((elem, attName, attValue) -> attName.startsWith("parse."));
        });

        xmlNormalizer.removeIgnorableWhitespace(elem -> //
                GraphmlElements.SET_OF_CONTENT_ELEMENTS.contains(elem.localName()));

        // sort /graphml/key elements by 'for' and then 'id' attribute
        {
            XmlElement rootElement = xmlNormalizer.doc().rootElement();
            sortKeyElements(rootElement);
        }

        {
            xmlNormalizer.doc().forEachMatchingElement(
                    xe ->
                            GraphmlElements.SET_OF_HAS_DATA_ELEMENTS.contains(
                                    xe.localName()), //
                    GraphmlNormalizer::sortDataElements);
        }

        // IMPROVE normalize space in data values

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
        List<XmlNode> list = xmlElement.childrenList();
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
        return xmlNormalizer.toXmlString();
    }

}
