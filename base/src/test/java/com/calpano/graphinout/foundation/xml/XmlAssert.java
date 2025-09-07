package com.calpano.graphinout.foundation.xml;

import static com.google.common.truth.Truth.assertThat;

public class XmlAssert {

    public static void xAssertThatIsSameXml(String actual, String expected) {
        String aNorm = XmlFormatter.normalize(actual);
        String aWrapped = XmlFormatter.wrap(aNorm, 100);

        String eNorm = XmlFormatter.normalize(expected);
        String eWrapped = XmlFormatter.wrap(eNorm, 100);

        assertThat(aWrapped).isEqualTo(eWrapped);
    }

}
