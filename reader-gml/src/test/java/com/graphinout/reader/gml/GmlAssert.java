package com.graphinout.reader.gml;

import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.stream.Collectors;

public class GmlAssert {


    public static void assertEquals(List<Object> expected, List<Object> actual) {
        String expectedStr = expected.stream().map(Object::toString).collect(Collectors.joining("\n"));
        String actualStr = actual.stream().map(Object::toString).collect(Collectors.joining("\n"));
        Assertions.assertEquals(expectedStr, actualStr);
    }

}
