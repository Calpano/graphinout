package com.graphinout.reader.ocif.cj;

import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;

import java.util.List;

public class OcifCj {


    public static class OcifInCj {

        /** OCIF extensions array */
        public static final String OCIF_EXTENSIONS = "ocif:extensions";

        /** OCIF doc-level data, see {@link OcifDocData} */
        public static final String OCIF_DOCUMENT = "ocif:document";
        /** OCIF node-level data, see {@link OcifNodeData} */
        public static final String OCIF_NODE = "ocif:node";

        public static final String OCIF_RELATION = "ocif:relation";

    }
    public static class CjInOcifData {

        /**
         * OCIF Data Extension object property for CJ data, in cases where CJ data is not an object.
         */
        public static final String DATA_NON_OBJECT = "cj:data";

    }

}
