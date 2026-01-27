package com.graphinout.reader.rdf;

import org.apache.jena.rdf.model.Model;
import org.jspecify.annotations.Nullable;

import static com.google.common.truth.Truth.assertThat;

public class RdfAssert {


    public static boolean xAssertThatIsSameRdf(Model actualRdfDoc, Model expectedRdfDoc, @Nullable Runnable extendedDebugInfos
    ) {
        String actualJson = RdfModels.toRdfNQuads(actualRdfDoc);
        String expectedJson = RdfModels.toRdfNQuads(expectedRdfDoc);
        return xAssertThatIsSameRdf(actualJson, expectedJson, extendedDebugInfos);
    }

    public static boolean xAssertThatIsSameRdf(String actualRdfNQuads, String expectedRdfNQuads, @Nullable Runnable extendedDebugInfos) {
        String expectedWrapped = RdfModels.normalize(actualRdfNQuads);
        String actualWrapped = RdfModels.normalize(expectedRdfNQuads);

        if (extendedDebugInfos != null && !actualWrapped.equals(expectedWrapped)) {
            extendedDebugInfos.run();
        }

        // prevent out of memory for large files
        if (actualRdfNQuads.length() < 1024 * 1024) {
            assertThat(actualWrapped).isEqualTo(expectedWrapped);
        } else {
            assertThat(actualWrapped.equals(expectedWrapped)).isTrue();
        }
        //test failed before, if it failed
        return true;
    }

}
