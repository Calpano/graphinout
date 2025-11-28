package com.graphinout.reader.gml;

import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.fail;

public class GmlAssert {

    public static void assertEqualsList(List<Object> expected, List<Object> actual) {
        if (expected.size() != actual.size()) {
            Assertions.fail(() -> {
                // actual - expected
                List<Object> tooMany = new ArrayList<>(actual);
                tooMany.removeAll(expected);

                List<Object> missing = new ArrayList<>(expected);
                missing.removeAll(actual);

                return "Expected size " + expected.size() + " but got " + actual.size() //
                        + "\n      EXPECT " + toString(expected)
                        + "\n      ACTUAL " + toString(actual)
                        + "\n     MISSING " + toString(missing)
                        + "\n  UNEXPECTED " + toString(tooMany)
                        ;
            });
        }
        expected.sort(Comparator.comparing(Object::toString));
        actual.sort(Comparator.comparing(Object::toString));
        for (int i = 0; i < expected.size(); i++) {
            Object expectedObj = expected.get(i);
            Object actualObj = actual.get(i);

            assertEqualsObject(expectedObj, actualObj);
        }
    }

    /**
     * @param expectedObj must be one of null, String, Number, boolean. Cannot be a list.
     * @param actualObj   same
     */
    public static void assertEqualsObject(Object expectedObj, Object actualObj) {
        if (expectedObj == null) {
            Assertions.assertNull(actualObj);
        } else if (expectedObj instanceof String expectedString) {
            Assertions.assertEquals(expectedString, actualObj);
        } else if (expectedObj instanceof Number expectedNumber) {
            Assertions.assertEquals(expectedNumber, actualObj);
        } else if (expectedObj instanceof Boolean expectedBoolean) {
            Assertions.assertEquals(expectedBoolean, actualObj);
        } else if (expectedObj instanceof GmlListHandler.Key expectedKey) {
            Assertions.assertEquals(expectedKey, actualObj);
        } else if (expectedObj instanceof GmlListHandler.Value expectedValue) {
            Assertions.assertEquals(expectedValue, actualObj);
        } else if (expectedObj instanceof GmlListHandler.Bracket expectedBracket) {
            Assertions.assertEquals(expectedBracket, actualObj);
        } else {
            fail("Unexpected object type: " + expectedObj.getClass().getSimpleName());
        }
    }

    public static void toHandler(List<Object> list, IGmlHandler handler) {
        for (Object o : list) {
            if (o instanceof GmlListHandler.Key(String key)) {
                handler.key(key);
            } else if (o instanceof GmlListHandler.Value(String value)) {
                handler.value(value);
            } else if (o instanceof GmlListHandler.Bracket bracket) {
                if (bracket == GmlListHandler.Bracket.Open) {
                    handler.open();
                } else {
                    handler.close();
                }
            } else {
                fail("Unexpected object type: " + o.getClass().getSimpleName());
            }
        }
    }

    public static String toString(List<Object> list) {
        GmlStringHandler handler = new GmlStringHandler();
        toHandler(list, handler);
        return handler.result();
    }

}
