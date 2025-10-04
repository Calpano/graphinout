package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.text.StringFormatter;
import com.calpano.graphinout.foundation.xml.element.XmlDocument;
import com.calpano.graphinout.foundation.xml.element.Xml2DocumentWriter;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class XmlFormatter {

    /**
     * A robust pattern to find and capture parts of an XML declaration. It handles variations in whitespace and ensures
     * matching quotes (single or double) are used.
     * <p>
     * Named Capture Groups:
     * <ul>
     *   <li>'version': The version value (e.g., "1.0").</li>
     *   <li>'encoding': The encoding value (e.g., "utf-8"), if present.</li>
     * </ul>
     */
    public static final Pattern XML_DECL_PATTERN = Pattern.compile("^<\\?xml\\s+version\\s*=\\s*(['\"])(?<version>[^'\"]+?)\\1" + // version (required) with named group
            "(?:\\s+encoding\\s*=\\s*(['\"])(?<encoding>[^'\"]+?)\\3)?" + // encoding (optional) with named group
            ".*?" + // a non-greedy match for other attributes like 'standalone'
            "\\?>", Pattern.CASE_INSENSITIVE);

    private static final Logger log = getLogger(XmlFormatter.class);


    /**
     * Canonicalize. Element order is strict and kept. Attribute order is sorted alphabetically. Comments are stripped.
     * Does not preserve whitespace.
     */
    public static String normalize(String rawXml) {
        Xml2StringWriter w = new Xml2StringWriter(Xml2AppendableWriter.AttributeOrderPerElement.Lexicographic, true);
        try {
            XmlTool.parseAndWriteXml(rawXml, w);
            return w.resultString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String normalizeAttributeOrder(String xml) {
        // read into doc
        Xml2DocumentWriter xmlWriter2XmlDocument = new Xml2DocumentWriter();
        try {
            XmlTool.parseAndWriteXml(xml, xmlWriter2XmlDocument);
            XmlDocument xmlDoc = xmlWriter2XmlDocument.resultDoc();
            // write to string with sorted atts
            Xml2StringWriter xml2string = new Xml2StringWriter(Xml2AppendableWriter.AttributeOrderPerElement.Lexicographic, true);
            if (xmlDoc != null) {
                xmlDoc.fire(xml2string);
            }
            return xml2string.resultString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String normalizeXmlDecl(String xml) {
        Matcher m = XML_DECL_PATTERN.matcher(xml);
        if (m.find()) {
            String version = m.group("version");
            // The 'encoding' group will be null if the attribute is not present
            String encoding = m.group("encoding");

            StringBuilder result = new StringBuilder();
            result.append("<?xml version=\"").append(version).append("\"");
            if (encoding != null) {
                result.append(" encoding=\"").append(encoding.toUpperCase(Locale.ENGLISH)).append("\"");
            }
            // Note: other attributes like 'standalone' are intentionally not preserved in the normalized form.
            result.append("?>");

            // Use Matcher.replaceFirst to safely replace only the matched declaration
            return m.replaceFirst(Matcher.quoteReplacement(result.toString())).trim();
        } else {
            return xml.trim();
        }
    }

    public static String simplifyForDebug(String in) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < in.length()) {
            int c = in.codePointAt(i);
            i += Character.charCount(c);
            switch (c) {
                case '\n' -> sb.append('N');
                case '\r' -> sb.append('R');
                case ' ' -> sb.append(" ");
                default -> sb.append("x");
            }
        }
        return sb.toString();
    }


    /**
     * Wrap after each '>' symbol AND long lines.
     *
     * @param raw        input
     * @param lineLength automatically enforced
     */
    public static String wrap(String raw, int lineLength) {
        String in = StringFormatter.normalizeLineBreaks(raw);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        StringBuilder line = new StringBuilder();
        while (i < in.length()) {
            int codePoint = in.codePointAt(i);
            line.appendCodePoint(codePoint);
            i += Character.charCount(codePoint);

            if (codePoint == '\n') {
                sb.append(line);
                line.setLength(0);
            } else if (codePoint == '>' || line.length() >= lineLength) {
                // insert additional linebreaks for readability
                sb.append(line);
                // try to peek next codepoint: if already a newline, skip inserting
                if (i == in.length() || in.codePointAt(i) == '\n') {
                    // skip at end or if next char is a newline
                } else {
                    sb.append("\n");
                }
                line.setLength(0);
            }
        }
        sb.append(line);
        return sb.toString();
    }


}
