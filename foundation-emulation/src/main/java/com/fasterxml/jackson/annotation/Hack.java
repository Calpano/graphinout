package com.fasterxml.jackson.annotation;

import java.util.Arrays;

public class Hack {

    public static String string_format(String s, Object... args) {
        return s + " args:" + Arrays.toString(args);
    }

}
