package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.CjGraphmlMapping;
import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.foundation.xml.AttMaps;
import com.calpano.graphinout.foundation.xml.NormalizingXmlWriter;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.calpano.graphinout.base.graphml.GraphmlElements.DESC;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPHML;
import static com.calpano.graphinout.base.graphml.GraphmlElements.KEY;

/**
 * {@link XmlWriter}that normalizes XML by sorting attributes and stripping comments
 */
public class NormalizingGraphmlWriter extends NormalizingXmlWriter<NormalizingGraphmlWriter> implements XmlWriter {

    static Set<String> skippedKeyIds = Set.of(
            CjGraphmlMapping.GraphmlDataElement.EdgeType.attrName,
            CjGraphmlMapping.GraphmlDataElement.Label.attrName,
            CjGraphmlMapping.GraphmlDataElement.CjJsonData.attrName,
            CjGraphmlMapping.GraphmlDataElement.SyntheticNode.attrName
            );

    public NormalizingGraphmlWriter(XmlWriter downstreamXmlWriter) {
        super(downstreamXmlWriter);

        // skip some <key> elements
        addAction(KEY, (ElementSkipAction) (name, atts) -> {
            String id = atts.get("id");
            return skippedKeyIds.contains(id);
        });

        // <desc> elements not deemed important for tests
        addAction(DESC, (ElementSkipAction) (name, atts) -> true);

        addAction(KEY, (AttributeAction) atts ->
                // remove redundant default <key for="all"/>
                AttMaps.removeIf(new TreeMap<>(atts), "for", "all"));

        addAction(GRAPHML, (AttributeAction) atts -> {
            Map<String, String> result = new TreeMap<>(atts);

            // auto-fix YWorks 1.0 namespace to GraphML 1.1
            AttMaps.changeIfPresent(result, XML.ATT_XMLNS, Graphml.XMLNS_YWORKS_1_0, Graphml.XMLNS_GRAPHML_1_x);
            result.putIfAbsent(XML.ATT_XMLNS, Graphml.XMLNS_GRAPHML_1_x);

            // set builtin GraphML namespace, schema location, if not yet defined
            result.putIfAbsent(XML.ATT_XMLNS_XSI, XML.XMLNS_XSI);
            AttMaps.changeIfPresent(result, XML.ATT_XSI_SCHEMA_LOCATION, val -> {
                String res = val;
                // replace all multiple spaces with single space
                res = res.replaceAll("\\s+", " ");
                // auto-upgrade GraphML 1.0 to 1.1
                res = res.replace(Graphml.XSI_SCHEMA_LOCATION_1_0, Graphml.XSI_SCHEMA_LOCATION_1_1);
                return res;
            });
            // if missing, set schema location to GraphML 1.1
            result.putIfAbsent(XML.ATT_XSI_SCHEMA_LOCATION, Graphml.XSI_SCHEMA_LOCATION_1_1);
            return result;
        });
        addAction(GRAPH, (AttributeAction) atts -> {
            TreeMap<String, String> sortedAtts = new TreeMap<>(atts);

            // remove redundant edgedefault="directed"
            AttMaps.removeIf(sortedAtts, "edgedefault", IGraphmlGraph.EdgeDefault.DEFAULT_EDGE_DEFAULT.graphmlString());

            // remove parseInfo, i.e. <graph> attributes names starting with "parse."
            for (String parseKey : sortedAtts.keySet().stream().filter(k -> k.startsWith("parse.")).toList()) {
                sortedAtts.remove(parseKey);
            }
            return sortedAtts;
        });

    }

}
