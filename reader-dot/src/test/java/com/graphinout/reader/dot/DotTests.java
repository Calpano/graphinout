package com.graphinout.reader.dot;

import com.graphinout.foundation.TestFileProvider;

import java.util.Set;
import java.util.stream.Stream;

import static com.graphinout.foundation.TestFileProvider.resources;

public class DotTests {

    /** includes all graphml files */
    public static Stream<TestFileProvider.TestResource> dotResources() {
        return resources("text/dot", Set.of(".dot", ".gv", ".gv.txt", ".dot.txt"));
    }

}

