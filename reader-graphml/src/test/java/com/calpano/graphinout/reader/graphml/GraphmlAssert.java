package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.foundation.xml.XmlFormatter;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertThat;

public class GraphmlAssert {

    public static boolean xAssertThatIsSameGraphml(String actual, String expected, @Nullable Runnable extendedDebugInfos) {

        String aNorm = GraphmlFormatter.normalize(actual);
        String aWrapped = XmlFormatter.wrap(aNorm, 100);

        String eNorm = GraphmlFormatter.normalize(expected);
        String eWrapped = XmlFormatter.wrap(eNorm, 100);

        if (extendedDebugInfos != null && !aWrapped.equals(eWrapped)) {
            extendedDebugInfos.run();
        }

        // prevent out of memory for guava-17 file (4,2 MB)
        if(actual.length() < 1024 * 1024) {
            assertThat(aWrapped).isEqualTo(eWrapped);
        } else {
            assertThat(aWrapped.equals(eWrapped)).isTrue();
        }
        //test failed before, if it failed
        return true;
    }


}
