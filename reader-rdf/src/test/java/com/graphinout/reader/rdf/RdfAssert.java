package com.graphinout.reader.rdf;

import org.apache.jena.rdf.model.Model;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class RdfAssert {

    private static final Logger log = getLogger(RdfAssert.class);

    public static boolean xAssertThatIsSameRdf(Model actualRdfDoc, Model expectedRdfDoc, @Nullable Runnable extendedDebugInfos) {
        if (actualRdfDoc.isIsomorphicWith(expectedRdfDoc)) {
            return true;
        }
        if (extendedDebugInfos != null) {
            extendedDebugInfos.run();
        }
        Model unexpected = actualRdfDoc.difference(expectedRdfDoc);
        Model missing = expectedRdfDoc.difference(actualRdfDoc);
        log.info("--- RDF expected, but missing:\n----\n" + RdfModels.toRdfNQuads(missing));
        log.info("--- RDF unexpected, but present:\n----\n" + RdfModels.toRdfNQuads(unexpected));
        return false;
    }

    public static boolean xAssertThatIsSameRdf(String actualRdfNQuads, String expectedRdfNQuads, @Nullable Runnable extendedDebugInfos) {
        Model actual = RdfModels.ofRdfNQuads(actualRdfNQuads);
        Model expected = RdfModels.ofRdfNQuads(expectedRdfNQuads);
        return xAssertThatIsSameRdf(actual, expected, extendedDebugInfos);
    }

}
