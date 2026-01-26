package com.graphinout.reader.rdf;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.Set;

import static com.graphinout.base.cj.document.CjUris.BLANK_NODE_PSEUDO_SCHEME;
import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;

public class RdfModel2CjDoc {

    private static void rdfModel2CjGraph(Model rdfModel, ICjDocumentMutable cjDoc, ICjGraphMutable cjGraph) {
        // Track which nodes have been explicitly created to avoid duplicates
        Set<String> createdNodes = new HashSet<>();

        // Iterate through all rdfTriples (triples)
        StmtIterator rdfStatements = rdfModel.listStatements();
        while (rdfStatements.hasNext()) {
            Statement stmt = rdfStatements.nextStatement();
            rdfStatementToCj(stmt, cjDoc, cjGraph, createdNodes);
        }
    }

    public static void rdfModel2cjDoc(Model rdfModel, ICjDocumentMutable cjDoc, String baseUri) {
        cjDoc.connectedJson(c -> {
            // TODO later
            c.canonical(false);
            c.versionDate(CjConstants.CJ_LATEST_VERSION_DATE);
            c.versionNumber(CjConstants.CJ_LATEST_VERSION_NUMBER);
        });
        // TODO use it to shorten URIs
        cjDoc.baseUri(baseUri);

        cjDoc.addGraph(cjGraph -> rdfModel2CjGraph(rdfModel, cjDoc, cjGraph));
    }

    private static void rdfStatementToCj(Statement stmt, ICjDocumentChunk cjDoc, ICjGraphMutable cjGraph, Set<String> createdNodes) {
        // == S
        Resource rdfSubject = stmt.getSubject();
        String subjectUri;
        if (rdfSubject.isAnon()) {
            subjectUri = BLANK_NODE_PSEUDO_SCHEME + rdfSubject.getId().getLabelString();
        } else {
            subjectUri = rdfSubject.getURI();
        }
        String subjectId = cjDoc.asId_(subjectUri);
        // == P
        Property predicate = stmt.getPredicate();
        String predicateId = cjDoc.asId_(predicate.getURI());
        // == O
        RDFNode object = stmt.getObject();

        // Check if this is an rdf:type triple
        if (RDF.type.equals(predicate) && object.isResource()) {
            // Map (subject, rdf:type, xxx) to a CJ node with types
            String objectUri = toUri(object.asResource());
            assert objectUri != null;
            String typeId = cjDoc.asId(objectUri);
            assert typeId != null;
            if (!createdNodes.contains(subjectId)) {
                cjGraph.addNode(cjNode -> {
                    cjNode.id(subjectId);
                    cjNode.addType(ICjElementType.of(typeId));
                });
                createdNodes.add(subjectId);
            } else {
                // Node already exists, need to add type to existing node
                cjGraph.nodes().filter(n -> subjectId.equals(n.id())).findFirst().ifPresent(cjNode -> {
                    cjNode.asNode().addType(ICjElementType.of(typeId));
                });
            }
        } else if (object.isResource()) {
            // Regular triple with resource object - create edge and explicit nodes
            String objectId;
            if (object.isURIResource()) {
                objectId = cjDoc.asId_(object.asResource().getURI());
            } else {
                assert object.isAnon();
                objectId = BLANK_NODE_PSEUDO_SCHEME + object.asResource().getId().getLabelString();
            }

            // Ensure both subject and object exist as explicit nodes (only if not already created)
            if (!createdNodes.contains(subjectId)) {
                cjGraph.addNode(cjNode -> cjNode.id(subjectId));
                createdNodes.add(subjectId);
            }
            if (!createdNodes.contains(objectId)) {
                cjGraph.addNode(cjNode -> cjNode.id(objectId));
                createdNodes.add(objectId);
            }

            // Create the edge
            ICjEdgeMutable cjEdge = cjGraph.addBiEdge(subjectId, objectId);
            cjEdge.edgeType(predicateId);
        } else if (object.isLiteral()) {
            // map to CJ data property
            Literal literal = object.asLiteral();
            if (!createdNodes.contains(subjectId)) {
                cjGraph.addNode(cjNode -> {
                    cjNode.id(subjectId);
                    cjNode.dataMutable(cjData -> cjData.add(pathOf(RdfCj.RdfInCj.rdfData, predicateId), literal.getLexicalForm()));
                });
                createdNodes.add(subjectId);
            } else {
                // Node already exists, need to add data to existing node
                // TODO: This is a limitation - we can't easily add data to existing nodes
                // For now, we skip adding the data
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private static String toUri(Resource resource) {
        if (resource.isAnon()) {
            return BLANK_NODE_PSEUDO_SCHEME + resource.getId().getLabelString();
        } else {
            return resource.getURI();
        }
    }

}
