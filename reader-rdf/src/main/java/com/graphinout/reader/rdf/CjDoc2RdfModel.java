package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjUris;
import com.graphinout.base.cj.document.ICjCoreElement;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

public class CjDoc2RdfModel {

    public static void cjDoc2Model(ICjDocument cjDoc, Model rdfModel) {
        cjDoc.graphs().forEach(cjGraph -> cjGraph2rdfModel(cjGraph, rdfModel));
    }

    private static void cjEdge2rdfModel(String effectiveBaseUri, ICjEdge cjEdge, Model rdfModel) {
        List<ICjEndpoint> endpoints = cjEdge.endpoints().toList();
        List<ICjEndpoint> sources = cjEdge.sources();
        List<ICjEndpoint> targets = cjEdge.targets();
        List<ICjEndpoint> undirectedEndpoints = cjEdge.undirectedEndpoints();

        String edgeType = cjEdge.type();

        if (endpoints.size() == 2 && sources.size() < 2 && targets.size() < 2) {
            // Order can be derived from endpoints
            ICjEndpoint sourceEndpoint;
            ICjEndpoint targetEndpoint;
            // 1 CJ Bi-edge => 1..3 RDF triples
            if (undirectedEndpoints.size() == 2) {
                // Cases: (A.undir, B.undir)
                sourceEndpoint = undirectedEndpoints.get(0);
                targetEndpoint = undirectedEndpoints.get(1);
                // sort by node id
                if (sourceEndpoint.node().compareTo(targetEndpoint.node()) > 0) {
                    ICjEndpoint tmp = sourceEndpoint;
                    sourceEndpoint = targetEndpoint;
                    targetEndpoint = tmp;
                }
            } else if (sources.size() == 1) {
                // Cases: (A.in, B.out), (A.in, B.undir), (A.out, B.in), (A.undir, B.in)
                sourceEndpoint = sources.getFirst();
                targetEndpoint = targets.size() == 1 ? targets.getFirst() : undirectedEndpoints.getFirst();
            } else {
                // Cases: (A.out, B.undir), (A.undir, B.out)
                sourceEndpoint = undirectedEndpoints.getFirst();
                targetEndpoint = targets.getFirst();
            }
            // edge is potentially
            // { "type": "EDGE_TYPE", "endpoints": [
            //   { "node": "SSS", "type": "SSS_TYPE", "direction": "in" }
            //   { "node": "TTT", "type": "TTT_TYPE",  "direction": "out" }
            // ] }
            // Triple: (SSS, EDGE_TYPE, TTT)
            Resource subject = cjNodeId2rdfResource(effectiveBaseUri, sourceEndpoint.node(), rdfModel);
            Resource object = cjNodeId2rdfResource(effectiveBaseUri, targetEndpoint.node(), rdfModel);
            {
                Property property = rdfModel.getProperty(nonNullOrDefault(edgeType, RdfCj.CjInRdf.IS_RELATED));
                rdfModel.add(subject, property, object);
            }
            ifPresentAccept(sourceEndpoint.type(), sssType -> {
                // Triple: (SSS, SSS_TYPE, TTT)
                Property property = rdfModel.getProperty(sssType);
                rdfModel.add(subject, property, object);
            });
            ifPresentAccept(targetEndpoint.type(), tttType -> {
                // Triple: (SSS, TTT_TYPE, TTT)
                Property property = rdfModel.getProperty(tttType);
                rdfModel.add(subject, property, object);
            });

        } else {
            // Cases: (A.in B.in), (A.out B.out) or != 2 endpoints
            // reify edge as resource and attach endpoints to it
            Resource edgeResource = cjElement2rdfResource(effectiveBaseUri, cjEdge, rdfModel);
            rdfModel.add(edgeResource, RDF.type, rdfModel.createResource(RdfCj.CjInRdf.CJ_EDGE));
            endpoints.forEach(ep -> {
                String pUri = RdfCj.CjInRdf.directionProperty(ep.direction());
                rdfModel.add(edgeResource, rdfModel.createProperty(pUri), cjNodeId2rdfResource(effectiveBaseUri, ep.node(), rdfModel));
            });

            // Generating triples from induced bi-edges
            // See https://j-s-o-n.org/connected-json/dev/rdf-interpretation/
            for (int i = 0; i < endpoints.size(); i++) {
                for (int j = 0; j < endpoints.size(); j++) {
                    if (i == j) continue;
                    ICjEndpoint ep1 = endpoints.get(i);
                    ICjEndpoint ep2 = endpoints.get(j);

                    // Determine predicate URI from endpoints or edge type
                    String predicateUri;
                    // Priority: endpoint type > edge type
                    if (ep2.type() != null) {
                        predicateUri = ep2.type();
                    } else if (ep1.type() != null) {
                        predicateUri = ep1.type();
                    } else if (cjEdge.edgeType() != null) {
                        predicateUri = cjEdge.type();
                    } else {
                        predicateUri = RdfCj.CjInRdf.IS_RELATED;
                    }
                    Resource subject = cjNodeId2rdfResource(effectiveBaseUri, ep1.node(), rdfModel);
                    Property predicate = rdfModel.createProperty(predicateUri);
                    Resource object = cjNodeId2rdfResource(effectiveBaseUri, ep2.node(), rdfModel);
                    rdfModel.add(subject, predicate, object);
                }
            }
        }
    }

    private static Resource cjElement2rdfResource(String baseUri, @NonNull ICjCoreElement cjCoreElement, Model rdfModel) {
        String uri = cjCoreElement.uri();
        if (uri.startsWith(CjUris.BLANK_NODE_PSEUDO_SCHEME)) {
            // extract stable blank node id
            return rdfModel.createResource(new AnonId(uri.substring(CjUris.BLANK_NODE_PSEUDO_SCHEME.length())));
        }
        // Regular node
        return rdfModel.createResource(uri);
    }

    public static void cjGraph2rdfModel(ICjGraph cjGraph, Model rdfModel) {
        String effectiveBaseUri = cjGraph.effectiveBaseUri();
        ;
        cjGraph.nodes().forEach(cjNode -> cjNode2rdfModel(effectiveBaseUri, cjNode, rdfModel));
        cjGraph.edges().forEach(cjEdge -> cjEdge2rdfModel(effectiveBaseUri, cjEdge, rdfModel));
    }

    private static void cjNode2rdfModel(String effectiveBaseUri, ICjNode cjNode, Model rdfModel) {
        Resource rdfSubject = cjElement2rdfResource(cjNode.effectiveBaseUri(), cjNode, rdfModel);
        rdfModel.add(rdfSubject, RDF.type, RDFS.Resource);

        // Add node types
        cjNode.types().forEach(cjType -> {
            Resource typeResource = rdfModel.createResource(cjType.type());
            rdfModel.add(rdfSubject, RDF.type, typeResource);
        });

        // Add node data properties
        cjNode.data(cjData -> {
            IJsonValue value = cjData.jsonValue();
            if (value != null) {
                if (value.isObject()) {
                    IJsonObject dataObject = value.asObject();
                    jsonObject2rdfModel(rdfSubject, dataObject, rdfModel);
                } else {

                }
            }
        });

        ICjData cjData = cjNode.data();
        if (cjData.isNotEmpty() && cjData.jsonValue_().isObject()) {
            IJsonObject dataObject = cjData.jsonValue_().asObject();
            dataObject.keys().forEach(key -> {
                // Check for the special "rdf:data" prefix
                if (key.startsWith(RdfCj.RdfInCj.rdfData + "/")) {
                    String predicateUri = key.substring(RdfCj.RdfInCj.rdfData.length() + 1);
                    Property predicate = rdfModel.createProperty(predicateUri);
                    Literal literal = rdfModel.createLiteral(dataObject.getString(key));
                    rdfModel.add(rdfSubject, predicate, literal);
                }
            });
        }
    }

    // FIXME really nullable? no
    private static Resource cjNodeId2rdfResource(String baseUri, @Nullable String cjNodeId, Model rdfModel) {
        if (cjNodeId == null) {
            // Blank node
            return rdfModel.createResource(AnonId.create());
        }
        String uri = CjUris.uri(baseUri, cjNodeId);
        if (uri.startsWith(CjUris.BLANK_NODE_PSEUDO_SCHEME)) {
            // extract stable blank node id
            return rdfModel.createResource(new AnonId(uri.substring(CjUris.BLANK_NODE_PSEUDO_SCHEME.length())));
        }
        // Regular node
        return rdfModel.createResource(cjNodeId);
    }

    private static ICjNode findNode(ICjDocument cjDoc, String nodeId) {
        return cjDoc.nodesAll().filter(n -> nodeId.equals(n.id())).findFirst().orElse(null);
    }

    private static void jsonObject2rdfModel(Resource rdfSubject, IJsonObject dataObject, Model rdfModel) {
        // emit as (s, cj:data, JSON literal)
        Property cj_rdfData = rdfModel.createProperty(RdfCj.CjInRdf.HAS_DATA);
        Literal literal = rdfModel.createTypedLiteral(dataObject.toJsonString(), RDF.dtRDFJSON);
        rdfModel.add(rdfSubject, cj_rdfData, literal);
//
//        if(dataObject.hasProperty(RdfCj.RdfInCj.rdfData)) {
//            // map RDF data transported in CJ back to the RDF it came from
//
//        }
//
//        dataObject.keys().forEach(key -> {
//            // Check for the special "rdf:data" prefix
//            if (key.startsWith(RdfCj.RdfInCj.rdfData + "/")) {
//                String predicateUri = key.substring(RdfCj.RdfInCj.rdfData.length() + 1);
//                Property predicate = rdfModel.createProperty(predicateUri);
//                Literal literal = rdfModel.createLiteral(dataObject.getString(key));
//                rdfModel.add(rdfSubject, predicate, literal);
//            } else {
//                // emit as (s, cj:data, JSON literal)
//                Property cj_rdfData = rdfModel.createProperty(RdfCj.CjInRdf.HAS_DATA);
//                Literal literal = rdfModel.createTypedLiteral(dataObject.toJsonString(), RDF.dtRDFJSON);
//                rdfModel.add(rdfSubject, cj_rdfData, literal);
//            }
//        });
    }

}
