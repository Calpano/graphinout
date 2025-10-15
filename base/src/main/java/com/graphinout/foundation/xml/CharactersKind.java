package com.graphinout.foundation.xml;

/** The different kinds of character data in XML */
public enum CharactersKind {
    /**
     * Characters may contain whitespace. Application decides if it is to be ignored. Contains no CDATA sections.
     */
    Default,
    /** like Default, but somehow we know whitespace should be preserved. Contains no CDATA sections. */
    PreserveWhitespace,
    /** Consists only of whitespace, which is somehow known as ignorable. Contains no CDATA sections. */
    IgnorableWhitespace,
    /** A CDATA section, which by definition always must preserve whitespace */
    CDATA
}
