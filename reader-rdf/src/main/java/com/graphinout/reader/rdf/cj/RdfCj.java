package com.graphinout.reader.rdf.cj;

import com.graphinout.base.cj.document.CjDirection;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jspecify.annotations.Nullable;

/** Expressing CJ in RDF */
public class RdfCj {

    public static class RdfInCj {

        public static String rdfData = "rdf:data";


    }

    public static class CjInRdf {

        public static String directionProperty(CjDirection cjDirection) {
            return VOC + switch (cjDirection) {
                case IN -> "hasIncomingEndpoint";
                case OUT -> "hasOutgoingEndpoint";
                case UNDIR -> "hasUndirectedEndpoint";
            };
        }

        /**
         * TODO content negotiation for an index.html and a vocab.rdf file
         */
        public static final String VOC = "http://j-s-o-n.org/connected-json/7.0.0/cj/";

        public static final String HAS_DATA = VOC + "hasData";

        public static final Property hasDataProperty = ResourceFactory.createProperty(HAS_DATA);

        public static final @Nullable String IS_RELATED = VOC + "isRelated";

        public static final String CJ_EDGE = VOC + "Edge";

    }

    public static final String BLANK_NODE_PSEUDO_SCHEME = "_:";

}
