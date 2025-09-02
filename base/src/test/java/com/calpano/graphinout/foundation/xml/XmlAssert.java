package com.calpano.graphinout.foundation.xml;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static org.apache.axiom.truth.xml.XMLTruth.xml;

public class XmlAssert {

    public static void xAssertThatIsSameXml(String actual, String expected) {
        String aNorm = XmlFormatter.normalize(actual);
        String aWrapped = XmlFormatter.wrap(aNorm, 100);

        String eNorm = XmlFormatter.normalize(expected);
        String eWrapped = XmlFormatter.wrap(eNorm, 100);

        assertThat(aWrapped).isEqualTo(eWrapped);

        assertAbout(xml()).that(actual) //
                .ignoringPrologAndEpilog().ignoringNamespaceDeclarations().ignoringNamespacePrefixes().ignoringWhitespace().hasSameContentAs(expected);

    }

}
