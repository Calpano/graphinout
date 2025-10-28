package com.graphinout.base.cj;

import com.graphinout.base.cj.element.CjDocuments;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.foundation.json.impl.JsonFormatter;
import io.github.classgraph.Resource;

import javax.annotation.Nullable;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.TestFileUtil.verifyOrRecord;

public class CjAssert {

    public static String normalize(String json) {
        String canonical = CjNormalizer.canonicalize(json);
        return JsonFormatter.formatDebug(canonical);
    }

    public static void verifySameCjOrRecord(Resource resource, String testId, String json_out, String json_in, @Nullable Runnable extendedDebugInfos) throws IOException {
        // we normalize the formatting before, so that --EXPECTED files on disk look nicer
        String expectedWrapped = JsonFormatter.formatDebug(json_in);
        // we send a formatted version of actual, so that RECORD_MODE=init writes THAT to disk
        String actualWrapped = JsonFormatter.formatDebug(json_out);
        verifyOrRecord(resource, testId, actualWrapped, expectedWrapped, (actual, expected) -> //
                CjAssert.xAssertThatIsSameCj(actual, expected, extendedDebugInfos), CjAssert::normalize);
    }

    public static boolean xAssertThatIsSameCj(ICjDocument actualCjDoc, ICjDocument expectedCjDoc, @Nullable Runnable extendedDebugInfos
    ) {
        String actualJson = CjDocuments.toJsonString(actualCjDoc);
        String expectedJson = CjDocuments.toJsonString(expectedCjDoc);
        return xAssertThatIsSameCj(actualJson, expectedJson, extendedDebugInfos);
    }

    public static boolean xAssertThatIsSameCj(String actualJson, String expectedJson, @Nullable Runnable extendedDebugInfos) {
        String expectedWrapped = normalize(expectedJson);
        String actualWrapped = normalize(actualJson);

        if (extendedDebugInfos != null && !actualWrapped.equals(expectedWrapped)) {
            extendedDebugInfos.run();
        }

        // prevent out of memory for large files
        if (actualJson.length() < 1024 * 1024) {
            assertThat(actualWrapped).isEqualTo(expectedWrapped);
        } else {
            assertThat(actualWrapped.equals(expectedWrapped)).isTrue();
        }
        //test failed before, if it failed
        return true;
    }

}
