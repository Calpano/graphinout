package com.graphinout.reader.ocif;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.foundation.pure.text.JsonFormatting;
import io.github.classgraph.Resource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.testdata.TestFileUtil.verifyOrRecord;

public class OcifAssert {

    private static final Logger log = LoggerFactory.getLogger(OcifAssert.class);

    public static String normalize(String ocifJson, int maxLineLength) {
        return OcifNormalizer.canonicalize(ocifJson, maxLineLength);
    }

    public static void verifySameOcifOrRecord(Resource resource, String testId, String json_out, String json_in, @Nullable Runnable extendedDebugInfos) throws IOException {
        // we normalize the formatting before, so that --EXPECTED files on disk look nicer
        String expectedWrapped = JsonFormatting.formatDebug(json_in);
        // we send a formatted version of actual, so that RECORD_MODE=init writes THAT to disk
        String actualWrapped = JsonFormatting.formatDebug(json_out);
        verifyOrRecord(resource, testId, actualWrapped, expectedWrapped, (actual, expected) -> //
                CjAssert.xAssertThatIsSameCj(actual, expected, extendedDebugInfos), CjAssert::normalize);
    }

    public static boolean xAssertThatIsSameOcif(String actualJson, String expectedJson, @Nullable Runnable extendedDebugInfos) {
        try {
            String expectedWrapped = normalize(expectedJson, 60);
            try {
                String actualWrapped = normalize(actualJson, 60);
                if (extendedDebugInfos != null && !actualWrapped.equals(expectedWrapped)) {
                    extendedDebugInfos.run();
                }

                // prevent out of memory for large files
                if (actualJson.length() < 1024 * 1024) {
                    assertThat(actualWrapped).isEqualTo(expectedWrapped);
                } else {
                    assertThat(actualWrapped.equals(expectedWrapped)).isTrue();
                }
            } catch (Exception e) {
                log.warn("Could not normalize actual OCIF/json:\n" + JsonFormatting.formatCompact(actualJson));
                Assertions.fail();
            }
        } catch (Exception e) {
            log.warn("Could not normalize expected OCIF/json:\n" + JsonFormatting.formatCompact(expectedJson));
            Assertions.fail();
        }

        //test failed before, if it failed
        return true;
    }


}
