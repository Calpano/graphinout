package com.graphinout.reader.rdf;

import org.apache.jena.rdf.model.Model;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import static com.graphinout.reader.rdf.RdfModels.normalize;
import static org.junit.jupiter.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;

public class RdfAssert {

    private static final Logger log = getLogger(RdfAssert.class);

    public static boolean xAssertThatIsSameRdf(Model actualRdf, Model expectedRdf, @Nullable Runnable extendedDebugInfos) {
        normalize(actualRdf);
        normalize(expectedRdf);
        if (actualRdf.isIsomorphicWith(expectedRdf)) {
            return true;
        }
        log.warn("RDF is not isomorphic.");
        if (extendedDebugInfos != null) {
            extendedDebugInfos.run();
        }
        Model unexpected = actualRdf.difference(expectedRdf);
        Model missing = expectedRdf.difference(actualRdf);
        if (!missing.isEmpty()) {
            log.info("--- RDF expected, but missing {} lines. First 40 lines:\n----\n{}", missing.size(), RdfModels.toRdfNQuads(missing, 40));
        }
        if (!unexpected.isEmpty()) {
            log.info("--- RDF unexpected, but present {} lines. First 40 lines:\n----\n{}", unexpected.size(), RdfModels.toRdfNQuads(unexpected, 40));
        }
        fail();
        return false;
    }

    public static boolean xAssertThatIsSameRdf(String actualRdfNQuads, String expectedRdfNQuads, @Nullable Runnable extendedDebugInfos) {
        Model actual = RdfModels.ofRdfNQuads(actualRdfNQuads);
        Model expected = RdfModels.ofRdfNQuads(expectedRdfNQuads);
        return xAssertThatIsSameRdf(actual, expected, extendedDebugInfos);
    }

}
