package com.graphinout.reader.rdf.cj;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jspecify.annotations.Nullable;

/** Expressing CJ in RDF */
public class RdfCj {

    public static class RdfInCj {

        public static String rdfData = "rdf:data";

    }

    public static class CjInRdf {

        /**
         * TODO content negotiation for an index.html and a vocab.rdf file
         */
        public static final String VOC = "http://j-s-o-n.org/connected-json/7.0.0/cj/";

        public static final String HAS_DATA = VOC +"hasData";

        public static final Property hasDataProperty = ResourceFactory.createProperty(HAS_DATA);

        public static final @Nullable String IS_RELATED = VOC+"isRelated";

    }

}
