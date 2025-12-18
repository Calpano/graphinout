package com.graphinout.foundation.pure.text;

import com.graphinout.foundation.pure.annotations.quality.QualitySuboptimal;

public class Texts {

    /** decimal: 13 */
    public static final char CR_13_R = '\r';
    /** decimal: 10 */
    public static final char LF_10_N = '\n';

    /**
     * Convert char to int, then to hex string, padding with leading zeros
     *
     * @param c char to convert
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

    /**
     * @return the common prefix of a and b
     */
    public static String commonPrefix(String a, String b) {
        String common = "";
        int maxLength = Math.min(a.length(), b.length());
        for (int i = 0; i < maxLength; i++) {
            if (a.charAt(i) == b.charAt(i)) {
                common += a.charAt(i);
            } else {
                break;
            }
        }
        return common;
    }

    @QualitySuboptimal
    public static String renderLogMessage(String s, Object[] o) {
        // replace each %s with a value from o array
        String msg = s;
        if (o != null) {
            for (Object x : o) {
                msg = msg.replace("{}", x.toString());
            }
        }
        return msg;
    }

}
