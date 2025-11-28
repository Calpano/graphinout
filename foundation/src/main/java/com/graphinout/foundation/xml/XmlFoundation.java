package com.graphinout.foundation.xml;

import java.util.regex.Pattern;

import static com.graphinout.foundation.util.Texts.CR_13_R;
import static com.graphinout.foundation.util.Texts.LF_10_N;

public class XmlFoundation {

    public static final Pattern P_TO_LF = Pattern.compile(
            // can be a prefix
            "(&#13;)?" + //
                    // order matters
                    "(" + CR_13_R + LF_10_N + "|" + CR_13_R + "|" + LF_10_N + ")" +

                    "|" + //

                    // can be standalone
                    "(&#13;)");

    @Deprecated
    public static String ampEncode(String raw) {
        return raw.replace("&", "&amp;");
    }

    /**
     * SAX Parser is running these decodes.
     *
     * @param characterData
     * @return
     */
    public static String xmlDecode(String characterData) {
        return characterData //
                .replace("&apos;", "'") //
                .replace("&quot;", "\"") //
                .replace("&lt;", "<") //
                .replace("&gt;", ">")

                // amp LAST is crucial
                .replace("&amp;", "&") //
                ;
    }

    /**
     * Encode the special chars <code>'"&<></code> as
     * <pre>
     *     &apos;, &quot;, &amp;, &lt;, &gt;
     * </pre>
     * <p>
     * <a
     * href="https://stackoverflow.com/questions/1091945/what-characters-do-i-need-to-escape-in-xml-documents/46637835#46637835">Source</a>
     *
     * <h2>Always</h2>
     * <p>
     * Escape '<' as '&lt;' unless '<' is starting a '<tag/>' or other markup. Escape '&' as '&amp;' unless '&' is
     * starting an '&entity;'. Escape control codes in XML 1.1 via Base64 or Numeric Character References.
     *
     * <h2>Attribute Values</h2>
     * attr=" 'Single quotes' are ok within double quotes." attr=' "Double quotes" are ok within single quotes.' Escape
     * " as &quot; and ' as &apos; otherwise.
     * <p>
     * In comments: no '--' allowed.
     * <p>
     * In CDATA: Escape ']]>' as ']]&gt;'.
     */
    public static String xmlEncode(String characterData) {
        if (characterData == null || characterData.isEmpty()) return "";


//        // amp signs are tricky: only replace with &amp; if not declaring an entity
//        // check string and find all sub-strings of characterData which are NOT a match for P_REF
//        // (i.e. all substrings which are not an entity reference)
//        Matcher matcher = P_REF.matcher(characterData);
//        int position = 0;
//        StringBuilder result = new StringBuilder();
//        while (matcher.find()) {
//            int start = matcher.start();
//            int end = matcher.end();
//            String before = characterData.substring(position, start);
//            result.append(ampEncode(before));
//            String s = matcher.group(0);
//            result.append(s);
//            position = end;
//        }
//        String remain = characterData.substring(position);
//        result.append(ampEncode(remain));

        String result = characterData;
        String res = result //
                // we must encode '&' round-tripping crap like '&amp;nbsp;'
                .replace("&", "&amp;") //

                .replace("'", "&apos;") //
                .replace("\"", "&quot;") //
                .replace("<", "&lt;") //
                .replace(">", "&gt;");
        return res;
    }

}
