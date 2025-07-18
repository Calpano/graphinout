package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.foundation.json.JsonType;

import java.util.Set;
import java.util.function.Predicate;

public class Util {

    public static CjType findExactlyOne(Set<CjType> set, JsonType jsonType) {
        CjType found = null;
        for (CjType element : set) {
            if (((Predicate<CjType>) (CjType type) -> type.hasJsonType(jsonType)).test(element)) {
                if (found != null) {
                    throw new IllegalArgumentException("Expected exactly one element matching " + jsonType + ", but found at least two in " + set);
                }
                found = element;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("Expected exactly one element matching " + jsonType + ", but found none in " + set);
        }
        return found;
    }

    /**
     * Is this a valid ISO language code as defined in <a href="https://datatracker.ietf.org/doc/html/rfc5646">RFC
     * 4646</a>?
     * <p>
     * Spec: A language tag is composed from a sequence of one or more "subtags", each of which refines or narrows the
     * range of language identified by the overall tag.  Subtags, in turn, are a sequence of alphanumeric characters
     * (letters and digits), distinguished and separated from other subtags in a tag by a hyphen ("-", [Unicode]
     * U+002D).
     * <p>
     * There are different types of subtag, each of which is distinguished by length, position in the tag, and content:
     * each subtag's type can be recognized solely by these features.  This makes it possible to extract and assign some
     * semantic information to the subtags, even if the specific subtag values are not recognized.  Thus, a language tag
     * processor need not have a list of valid tags or subtags (that is, a copy of some version of the IANA Language
     * Subtag Registry) in order to perform common searching and matching operations.  The only exceptions to this
     * ability to infer meaning from subtag structure are the grandfathered tags listed in the productions 'regular' and
     * 'irregular' below.  These tags were registered under [RFC3066] and are a fixed list that can never change.
     */
    public static boolean isIsoLangCode(String s) {
        // Regular expression for ISO 639-1 (2-letter) or ISO 639-2/3 (3-letter) language codes
        // followed by an optional country code (ISO 3166-1 alpha-2)
        // Examples: "en", "en-US", "eng", "eng-GB"
        return s.matches("^[a-z]{2,3}(-[A-Z]{2})?$");
    }

}
