package com.graphinout.base.xml.util;

import com.graphinout.foundation.pure.xml.writer.Xml2AppendableWriter;
import com.graphinout.foundation.pure.xml.writer.XmlWriter;
import io.github.classgraph.Resource;

import java.nio.file.Path;

public class XmlTestTool {

    /** check if that is valid XML to begin with */
    public static void assertCanParseAsXml(Resource xmlResource) throws Exception {
        parseAndWriteXml(xmlResource, Xml2AppendableWriter.createNoop());
    }

    /**
     * check if that is valid XML to begin with. CAUTION: File-based reading can fail in a CI/CD pipeline. Better use
     * {@link XmlTestTool#assertCanParseAsXml(Resource)}
     */
    public static void assertCanParseAsXml(Path xmlFilePath) throws Exception {
        XmlTool.parseAndWriteXml(xmlFilePath.toFile(), Xml2AppendableWriter.createNoop());
    }

    public static void parseAndWriteXml(Resource xmlResource, XmlWriter xmlWriter) throws Exception {
        String xmlString = xmlResource.getContentAsString();
        XmlTool.parseAndWriteXml(xmlResource.getURI().toString(), xmlString, xmlWriter);
    }

}
