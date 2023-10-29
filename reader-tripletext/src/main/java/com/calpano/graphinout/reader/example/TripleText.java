package com.calpano.graphinout.reader.example;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TripleText {

    static final String SPACE = "[ \t]*+";
    static final Pattern p = Pattern.compile(SPACE  //
            // subject
            + "(?<s>.*?)"  //
            + SPACE
            // --predicate--
            + "--" + SPACE + "(?<p>.*?)" + SPACE + "--" + SPACE  //
            // object
            + "(?<o>"
            // negative lookahead
            + "(?:.(?![.][.]))"  //
            + "*)" +
            // .. meta
            SPACE + "([.][.]" + SPACE + "(?<m>.*?)" + ")?" + SPACE);

    public static void parseLine(String line, ITripleHandler<String, String, String> handler) {
        Matcher m = p.matcher(line);
        if (m.matches()) {
            handler.onTriple(m.group("s"), m.group("p"), m.group("o"), m.group("m"));
        }
    }

    /**
     * Bad code, but good for using in tests.
     */
    public static Triple<String, String, String> parseToTriple(String line) {
        AtomicReference<Triple<String, String, String>> t = new AtomicReference<>();
        parseLine(line, (s, p, o, m) -> t.set(new Triple<>(s, p, o, m)));
        return t.get();
    }


}
