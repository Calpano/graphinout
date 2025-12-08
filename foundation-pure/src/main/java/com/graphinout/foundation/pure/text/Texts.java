package com.graphinout.foundation.pure.text;

public class Texts {

    /** decimal: 13 */
    public static final char CR_13_R = '\r';
    /** decimal: 10 */
    public static final char LF_10_N = '\n';

    /**
     * Convert char to int, then to hex string, padding with leading zeros
     *
     * @param c
     * @return e.g. {code\\u0034} (just one backslash)
     */
    public static String asUnicodeEscape(int c) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\u");
        // Convert char to int, then to hex string, padding with leading zeros
        String hex = Integer.toHexString(c);
        for (int j = 0; j < 4 - hex.length(); j++) {
            sb.append('0');
        }
        sb.append(hex);
        return sb.toString();
    }

}
