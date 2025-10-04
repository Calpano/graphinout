package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.foundation.xml.XmlFormatter;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertThat;

public class GraphmlAssert {

    public static boolean xAssertThatIsSameGraphml(String actual, String expected, @Nullable Runnable extendedDebugInfos) {

        String actualNorm = new GraphmlNormalizer(actual).resultString();
        String actualWrapped = XmlFormatter.wrap(actualNorm, 100);

        String expectedNorm = new GraphmlNormalizer(expected).resultString();
        String expectedWrapped = XmlFormatter.wrap(expectedNorm, 100);

        if (extendedDebugInfos != null && !actualWrapped.equals(expectedWrapped)) {
            extendedDebugInfos.run();
        }

        // prevent out of memory for guava-17 file (4,2 MB)
        if(actual.length() < 1024 * 1024) {
            assertThat(actualWrapped).isEqualTo(expectedWrapped);
        } else {
            assertThat(actualWrapped.equals(expectedWrapped)).isTrue();
        }
        //test failed before, if it failed
        return true;
    }


}
