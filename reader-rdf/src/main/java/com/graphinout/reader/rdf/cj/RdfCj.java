package com.graphinout.reader.rdf.cj;

import com.graphinout.base.cj.document.CjDirection;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jspecify.annotations.Nullable;

/** Expressing CJ in RDF */
public class RdfCj {

    public static class RdfInCj {

        public static String rdfData = "rdf:data";

        /** Edge-data key for structured per-link props (ddot {@code ,, ..key.. value}); a nested key→value object. */
        public static final String LINK_PROPS = "ddot-it:props";

        /** Edge-data key for free-text per-link notes (ddot {@code ,, note}); a string or array of strings. */
        public static final String LINK_TEXT = "ddot-it:text";

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
        public static final String VOC = "http://j-s-o-n.org/connected-json/8.0.0/cj/";

        public static final String HAS_DATA = VOC + "hasData";

        public static final Property hasDataProperty = ResourceFactory.createProperty(HAS_DATA);

        public static final @Nullable String IS_RELATED = VOC + "isRelated";

        public static final String CJ_EDGE = VOC + "Edge";

        /**
         * Predicate carrying a free-text note (ddot {@code ddot-it:text}) for a statement, used as the
         * metadata predicate on the RDF-star reifier of the annotated base triple.
         */
        public static final String HAS_NOTE = VOC + "hasNote";

        /**
         * Sentinel subject IRI for CJ document-level data ({@code ddot.it/this}). Fixed (independent of the
         * base URI) so document-data triples round-trip regardless of the document's location.
         */
        public static final String THIS_DOCUMENT = VOC + "thisDocument";

    }

}
