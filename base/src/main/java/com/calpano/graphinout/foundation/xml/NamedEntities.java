package com.calpano.graphinout.foundation.xml;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedEntities {

    public static final String ENTITY_PATTERN = "&" +
            "(?:([a-zA-Z]+)" +
            "|" +
            "#" +
            "(?:x" +
            "([0-9a-fA-F]+)" +
            "|" +
            "([0-9]+)" +
            ")" +
            ");";
    // The 5 XML predefined entities
    final static Set<String> xmlEntities = Set.of("amp", "lt", "gt", "quot", "apos");
    /**
     * NameStartChar  ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] |
     * [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] |
     * [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     */
    static String CHARACTERS_NAMESTART = ":_" + "a-z" + "A-Z" + "\\u00C0-\\u00D6" + "\\u00D8-\\u00F6" + "\\u00F8-\\u02FF" + "\\u0370-\\u037D" + "\\u037F-\\u1FFF" + "\\u200C-\\u200D" + "\\u2070-\\u218F" + "\\u2C00-\\u2FEF" + "\\u3001-\\uD7FF" + "\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD" + "\\x{10000}-\\x{EFFFF}";
    /**
     * NameChar   ::=  NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     */
    static String CHARACTERS_NAME = "-.0-9" + "\\u00B7" + "\\u0300-\\u036F" + "\\u203F-\\u2040" + CHARACTERS_NAMESTART;
    /**
     * Name   ::= NameStartChar (NameChar)*
     */
    static String NAME = "[" + CHARACTERS_NAME + "]";
    static String NAMESTART = "[" + CHARACTERS_NAMESTART + "]";
    /** EntityRef := '&' Name ';' */
    static Pattern P__ENTITYREF = Pattern.compile("&" + NAMESTART + "(" + NAME + ")*;");
    /** CharRef  ::=   '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';' */
    static Pattern P__CHARREF = Pattern.compile("&#([0-9]+|#x([0-9a-fA-F]+));");
    /** any ref */
    static Pattern P_REF = Pattern.compile(P__ENTITYREF.pattern() + "|" + P__CHARREF.pattern());

    /**
     * @param namedEntityFun   from HTML entity name to replacement, e.g. from 'Eacute' to '{@code &#201;}'
     * @param numericEntityFun from HTML numeric entity to replacement, e.g. from '&#233;' to '{@code &#201;}'
     */
    public static String htmlEntitiesTo(String xmlContent, Function<String, String> namedEntityFun,
                                        Function<Integer, String> numericEntityFun
    ) {
        Pattern entityPattern = Pattern.compile(ENTITY_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = entityPattern.matcher(xmlContent);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(1) != null) { // named entity
                String name = matcher.group(1);
                boolean isXmlEntity = xmlEntities.contains(name.toLowerCase());
                if (isXmlEntity) {
                    // no need to simplify
                    matcher.appendReplacement(sb, "&" + name + ";");
                } else if (HtmlEntities.contains(name)) {
                    String replacement = namedEntityFun.apply(name);
                    assert replacement != null;
                    matcher.appendReplacement(sb, replacement);
                } else {
                    // leave unknown as is
                    matcher.appendReplacement(sb, "&" + name + ";");
                }
            } else if (matcher.group(2) != null) { // hex
                int charValue = Integer.parseInt(matcher.group(2), 16);
                String replacement = numericEntityFun.apply(charValue);
                matcher.appendReplacement(sb, replacement);
            } else if (matcher.group(3) != null) { // decimal
                int charValue = Integer.parseInt(matcher.group(3));
                String replacement = numericEntityFun.apply(charValue);
                matcher.appendReplacement(sb, replacement);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * '{@code &Eacute;}' becomes '{@code &amp;Eacute;}'.
     */
    public static String htmlEntitiesToAmpEncoded(String xmlContent) {
        return htmlEntitiesTo(xmlContent, name -> "&amp;" + name + ";",
                // keep
                entityNum -> "&#" + entityNum + ";"
        );
    }

    /**
     * This allows HTML-like XML (including things like an '{@code &nbsp;}') to be parsed by SAX parsers.
     * <p>
     * Replace all HTML entities with their Unicode entity equivalents, except the 5 XML predefined entities. E.g.
     * '{@code &Eacute;}' becomes '{@code &#201;}'.
     *
     * @param xmlContent which may contain (X)HTML entities
     * @return content with the HTML entities replaced
     */
    public static String htmlEntitiesToDecimalEntities(String xmlContent) {
        return htmlEntitiesTo(xmlContent, HtmlEntities::getDecimalReplacement,
                // keep them as is
                entityNum -> "&#" + entityNum + ";");
    }

}
