package com.graphinout.reader.rdf;

import com.graphinout.testdata.TestFileProvider;

import java.util.Set;
import java.util.stream.Stream;

public class RdfResources {

    public static Stream<TestFileProvider.TestResource> rdfResources(RdfFormats.RdfSyntax syntax) {
        return TestFileProvider.resources(syntax.resourcePath, Set.of());
    }


}
