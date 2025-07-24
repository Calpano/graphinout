package com.calpano.graphinout.foundation;

public class StringFormatter {

    /**
     * Simple, hard break exactly at lineLength
     * @param in input
     * @param lineLength automatically enforced
     */
    public static String wrapped(String in, int lineLength) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < in.length()) {
            if (i + lineLength < in.length()) {
                sb.append(in, i, i + lineLength).append("\n");
            } else {
                sb.append(in.substring(i));
            }
            i += lineLength;
        }
        return sb.toString();
    }

}
