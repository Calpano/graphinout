package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.foundation.xml.AttMaps;
import com.calpano.graphinout.foundation.xml.NormalizingXmlWriter;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import java.util.Map;
import java.util.TreeMap;

import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPHML;
import static com.calpano.graphinout.base.graphml.GraphmlElements.KEY;

/**
 * {@link XmlWriter}that normalizes XML by sorting attributes and stripping comments
 */
public class NormalizingGraphmlWriter extends NormalizingXmlWriter<NormalizingGraphmlWriter> implements XmlWriter {

    public NormalizingGraphmlWriter(XmlWriter downstreamXmlWriter) {
        super(downstreamXmlWriter);
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
        addAction(GRAPH, (AttributeAction) atts ->
                // remove redundant default <graph edgedefault="undirected"/>
                AttMaps.removeIf(new TreeMap<>(atts), "edgedefault", "undirected"));

        addAction(KEY, (AttributeAction) atts ->
                // remove redundant default <key for="all"/>
                AttMaps.removeIf(new TreeMap<>(atts), "for", "all"));
    }

}
