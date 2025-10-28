package com.graphinout.foundation.xml;

import com.graphinout.foundation.text.ITextWriter;
import com.graphinout.foundation.text.TextReader;
import com.graphinout.foundation.text.TextWriterOnStringBuilder;
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
     * String-based processing, no XML parsing
     * @return {#code `<?xml version="...+ encoding="...UPPERCASED...">`}
     */
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

    /**
     * String-based processing, no XML parsing
     */
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
     * String-based processing, no XML parsing
     *
     * @param raw        input
     * @param lineLength automatically enforced
     */
    public static String wrap(String raw, int lineLength) {
        StringBuilder result = new StringBuilder();
        ITextWriter resultWriter = new TextWriterOnStringBuilder(result);

        TextReader.read(raw, rawLine -> {
            StringBuilder line = new StringBuilder();
            TextReader.forEachCodepointWithIndexAndLookAhead(rawLine, (cp, index, cpNext) -> {
                line.appendCodePoint(cp);
                if (cp == '>' || line.length() >= lineLength || cpNext == -1) {
                    // insert additional linebreaks for readability: finnish current line and start a new one
                    resultWriter.line(line.toString().trim());
                    line.setLength(0);
                }
            });
            // leftovers
            if (!line.isEmpty()) {
                resultWriter.line(line.toString().trim());
            }
        });

        return result.toString();
    }


}
