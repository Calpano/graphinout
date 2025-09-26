package com.calpano.graphinout.foundation.xml;

import static com.google.common.truth.Truth.assertThat;

public class XmlAssert {

    private static final int LIMIT = 1024 * 1024;

    public static void xAssertThatIsSameXml(String actual, String expected) {
        String aNorm = XmlFormatter.normalize(actual);
        String aWrapped = XmlFormatter.wrap(aNorm, 100);

        String eNorm = XmlFormatter.normalize(expected);
        String eWrapped = XmlFormatter.wrap(eNorm, 100);

        // cannot compute diff on long content
        if(aWrapped.length() > LIMIT || eWrapped.length() > LIMIT) {
            // user simpler means
            assertThat(aWrapped.equals(eWrapped)).isTrue();
        } else {
            assertThat(aWrapped).isEqualTo(eWrapped);
        }

    }

}
