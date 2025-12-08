package com.graphinout.foundation.pure.text;

public class UnicodesFoundation {

    public enum KindOfCharacter {
        Digit, Lowercase, None, Uppercase
    }

    /**
     * @param hex must fit in one Integer
     * @return an unsigned int or throws
     */
    public static int fromHex(final String hex) {
        return Integer.parseInt(hex, 16);
        // IMPROVE: return Integer.parseUnsignedInt(hex, 16);
    }

    /**
     * @param uPlusCode the syntax used by Unicode consortium, e.g. 'U+0020' (space)
     * @return codepoint
     */
    public static int fromUplusCode(final String uPlusCode) {
        assert uPlusCode.startsWith("U+");
        final String hex = uPlusCode.substring(2);
        return fromHex(hex);
    }

    public static String toHex(final int codePoint) {
        return Integer.toHexString(codePoint);
    }

    /**
     * Using UTF-16 a surrogate pair if required
     *
     * @param codepoint
     * @return
     */
    public static String toString(final int codepoint) {
        return new String(toSurrogatePair(codepoint));
    }

    /**
     * 0x010000 is subtracted from the code point, leaving a 20-bit number in the range 0x000000..0x0FFFFF.
     * <p>
     * The top ten bits (a number in the range 0x0000..0x03FF) are added to 0xD800 to give the first 16-bit code unit or
     * high surrogate, which will be in the range 0xD800..0xDBFF.
     * <p>
     * The low ten bits (also in the range 0x0000..0x03FF) are added to 0xDC00 to give the second 16-bit code unit or
     * low surrogate, which will be in the range 0xDC00..0xDFFF.
     *
     * @param codepoint
     * @return an array of length 1 or 2, depending if a surrogate pair is needed
     */
    public static char[] toSurrogatePair(final int codepoint) {
        if (codepoint < 0x010000) {
            return new char[]{(char) codepoint};
        }
        final int c = codepoint - 0x010000;
        final char topTenBits = (char) ('\uD800' + (char) (c >>> 10));
        // low ten bits = 3, F (4 bits), F (4 bits)
        final char lowTenBits = (char) ('\uDC00' + (c & 0x3FF));
        return new char[]{topTenBits, lowTenBits};
    }

}
