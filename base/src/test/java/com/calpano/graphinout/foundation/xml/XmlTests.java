package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.xml.Xml2XmlStringWriterTest.Input_Expected;

import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.util.Texts.CR_13_R;
import static com.calpano.graphinout.foundation.util.Texts.LF_10_N;
import static com.calpano.graphinout.foundation.xml.Xml2XmlStringWriterTest.Input_Expected.input_expected;

public class XmlTests {

    /** numeric entity 13 aka CR */
    public static final String NE_13_CR = "&#13;";

    public static String wrapInRoot(String s) {
        return XML.XML_VERSION_1_0_ENCODING_UTF_8 + "\n<root>" + s + "</root>";
    }

    /**
     * XML element text content input string -- expected in-memory decoded string from SAX parser. The test must wrap
     * these in 'XML DECL (rootElem) testString (/rootElem)'.
     *
     * <h2>SAX Parser Line Endings</h2>
     * CR LF -> LF; CR -> LF;
     * <p>
     * #13 CR -> CR CR -> LF
     */
    public static Stream<Input_Expected> xmlStringsSaxParsed() {
        return Stream.of( //
                // line break issues
                input_expected( // TEXT CR -> TEXT LF
                        "a1aa" + CR_13_R //
                                + "bbb" + CR_13_R //
                                + "ccc" + CR_13_R, //
                        "a1aa" + LF_10_N //
                                + "bbb" + LF_10_N//
                                + "ccc" + LF_10_N), //
                input_expected( // TEXT CR CR -> TEXT LF LF
                        "a11aa" + CR_13_R + CR_13_R //
                                + "bbb" + CR_13_R + CR_13_R //
                                + "ccc" + CR_13_R + CR_13_R, //
                        "a11aa" + LF_10_N + LF_10_N //
                                + "bbb" + LF_10_N + LF_10_N//
                                + "ccc" + LF_10_N + LF_10_N), //
                input_expected( //  TEXT LF -> TEXT LF
                        "a2aa" + LF_10_N //
                                + "bbb" + LF_10_N //
                                + "ccc" + LF_10_N, //
                        "a2aa" + LF_10_N //
                                + "bbb" + LF_10_N//
                                + "ccc" + LF_10_N), //
                input_expected( // TEXT CR LF -> TEXT LF
                        "a3aa" + CR_13_R + LF_10_N //
                                + "bbb" + CR_13_R + LF_10_N //
                                + "ccc" + CR_13_R + LF_10_N, //
                        "a3aa" + LF_10_N //
                                + "bbb" + LF_10_N//
                                + "ccc" + LF_10_N), //

                input_expected( // TEXT #13 CR -> TEXT 'CR' CR -> TEXT LF
                        "aX1aa" + NE_13_CR + CR_13_R //
                                + "bbb" + NE_13_CR + CR_13_R //
                                + "ccc" + NE_13_CR + CR_13_R, //
                        "aX1aa" + LF_10_N //
                                + "bbb" + LF_10_N//
                                + "ccc" + LF_10_N), //
                input_expected( // TEXT #13 LF -> TEXT 'CR' LF -> TEXT LF
                        "aX2aa" + NE_13_CR + LF_10_N //
                                + "bbb" + NE_13_CR + LF_10_N //
                                + "ccc" + NE_13_CR + LF_10_N, //
                        "aX2aa" + LF_10_N  //
                                + "bbb" + LF_10_N//
                                + "ccc" + LF_10_N), //
                input_expected( // TEXT #13 CR LF -> TEXT 'CR' CR LF -> TEXT LF
                        // This looks like 'Goobi-Topologie.graphml.xml'
                        "aX3aa" + NE_13_CR + CR_13_R + LF_10_N //
                                + "bbb" + NE_13_CR + CR_13_R + LF_10_N //
                                + "ccc" + NE_13_CR + CR_13_R + LF_10_N, //
                        "aX3aa" + LF_10_N  //
                                + "bbb" + LF_10_N//
                                + "ccc" + LF_10_N), //
                input_expected( // TEXT #13 -> TEXT LF
                        "aX4aa" + NE_13_CR//
                                + "bbb" + NE_13_CR //
                                + "ccc" + NE_13_CR, //
                        "aX4aa" + LF_10_N //
                                + "bbb" + LF_10_N//
                                + "ccc" + LF_10_N),

                // summary of observations

                // TEXT CR        -> TEXT LF
                // TEXT LF        -> TEXT LF
                // TEXT CR LF     -> TEXT LF
                // TEXT #13       -> TEXT LF
                // TEXT #13 CR    -> TEXT LF
                // TEXT #13 LF    -> TEXT LF
                // TEXT #13 CR LF -> TEXT LF

                // TEXT CR CR     -> TEXT LF LF

                // == other issues
                // "&Eacute;" -preprocessing-> "&amp;Eacute;" -parsing-> "&Eacute;"
                input_expected("&Eacute;", "&Eacute;"), //
                input_expected("aaa", "aaa"), //
                input_expected("äää", "äää"), //
                input_expected("a\nb", "a\nb"), //
                input_expected("&amp;", "&"), //
                input_expected("&quot;", "\""), //
                input_expected("&apos;", "'"), //
                input_expected("&lt;", "<"), //
                input_expected("&gt;", ">"), //
                input_expected("\"", "\""), //
                input_expected("'", "'") //
        );
    }

}
