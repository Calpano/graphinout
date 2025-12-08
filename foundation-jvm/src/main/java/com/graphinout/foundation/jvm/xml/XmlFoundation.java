package com.graphinout.foundation.jvm.xml;

import java.util.regex.Pattern;

import static com.graphinout.foundation.pure.text.Texts.CR_13_R;
import static com.graphinout.foundation.pure.text.Texts.LF_10_N;

public class XmlFoundation {

    public static final Pattern P_TO_LF = Pattern.compile(
            // can be a prefix
            "(&#13;)?" + //
                    // order matters
                    "(" + CR_13_R + LF_10_N + "|" + CR_13_R + "|" + LF_10_N + ")" +

                    "|" + //

                    // can be standalone
                    "(&#13;)");

}
