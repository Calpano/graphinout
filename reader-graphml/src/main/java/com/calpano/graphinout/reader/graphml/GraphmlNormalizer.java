package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement;
import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlNormalizer;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.util.Set;

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
            if (!elem.localName().equals(KEY))
                return false;
            String id = elem.attribute("id");
            return id != null && skippedKeyIds.contains(id);
        });

        // <desc> elements not deemed important for tests
        xmlNormalizer.removeElementIf(elem -> elem.localName().equals(DESC));

        // remove redundant default <key for="all"/>
        xmlNormalizer.removeAttributeIf((elem, attName, attValue) -> elem.localName().equals(KEY) && attName.equals("for") && attValue.equals("all"));

        // remove graph.edgedefault
        xmlNormalizer.removeAttributeIf((elem, attName, attValue) -> elem.localName().equals(GRAPH) && attName.equals(
                IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT));

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
                GraphmlElements.setOfContentElementNames().contains(elem.localName()));
    }

    public String resultString() {
        return xmlNormalizer.toXmlString();
    }

}
