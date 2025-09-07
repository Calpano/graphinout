package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.foundation.xml.XmlFormatter;

import static com.google.common.truth.Truth.assertThat;

public class GraphmlAssert {

    public static void xAssertThatIsSameGraphml(String actual, String expected) {

        String aNorm = GraphmlFormatter.normalize(actual);
        String aWrapped = XmlFormatter.wrap(aNorm, 100);

        String eNorm = GraphmlFormatter.normalize(expected);
        String eWrapped = XmlFormatter.wrap(eNorm, 100);

        assertThat(aWrapped).isEqualTo(eWrapped);
    }


}
