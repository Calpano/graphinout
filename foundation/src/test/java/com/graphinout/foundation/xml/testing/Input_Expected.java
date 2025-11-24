package com.graphinout.foundation.xml.testing;

public record Input_Expected(String input, String expected) {

    public static Input_Expected input_expected(String input, String expected) {
        return new Input_Expected(input, expected);
    }

}
