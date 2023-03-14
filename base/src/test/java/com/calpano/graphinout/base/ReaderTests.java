package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.ValidatingGraphMlWriter;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.xml.ValidatingXmlWriter;
import com.calpano.graphinout.base.xml.XmlWriter;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ReaderTests {

    private static final Logger log = getLogger(ReaderTests.class);

    public static boolean canRead(GioReader gioReader, String resourcePath) {
        return gioReader.fileFormat().fileExtensions().stream().anyMatch(resourcePath::endsWith);
    }

    public static GioWriter createWriter(OutputSink outputSink, boolean validateXml, boolean validateGraphml, boolean validateGio) {
        XmlWriter xmlWriter = new XmlWriterImpl(outputSink);
        if (validateXml) {
            xmlWriter = new ValidatingXmlWriter(xmlWriter);
        }
        GraphmlWriter graphmlWriter = new GraphmlWriterImpl(xmlWriter);
        if (validateGraphml) {
            graphmlWriter = new ValidatingGraphMlWriter(graphmlWriter);
        }
        GioWriter gioWriter = new GioWriterImpl(graphmlWriter);
        if (validateGio) {
            // TODO        gioWriter = new ValidatingGioWriter( gioWriter );
        }
        return gioWriter;
    }

}
