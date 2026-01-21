package com.graphinout.reader.rdf;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.CjUris;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;

public class RdfModel2CjDoc {

    private static void rdfModel2CjGraph(Model rdfModel, ICjDocumentMutable cjDoc, ICjGraphMutable cjGraph) {
        // Iterate through all rdfTriples (triples)
        StmtIterator rdfStatements = rdfModel.listStatements();
        while (rdfStatements.hasNext()) {
            Statement stmt = rdfStatements.nextStatement();
            rdfStatementToCj(stmt, cjDoc, cjGraph);
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

    private static void rdfStatementToCj(Statement stmt, ICjDocumentChunk cjDoc, ICjGraphMutable cjGraph) {
        // == S
        Resource subject = stmt.getSubject();
        String subjectUri;
        if (subject.isAnon()) {
            subjectUri = CjUris.BLANK_NODE_PSEUDO_SCHEME + subject.getId().getLabelString();
        } else {
            subjectUri = subject.getURI();
        }
        String subjectId = cjDoc.asId(subjectUri);
        // == P
        Property predicate = stmt.getPredicate();
        String predicateId = cjDoc.asId(predicate.getURI());
        // == O
        RDFNode object = stmt.getObject();
        if (object.isResource()) {
            String objectId;
            if (object.isURIResource()) {
                objectId = cjDoc.asId(object.asResource().getURI());
            } else {
                assert object.isAnon();
                objectId = CjUris.BLANK_NODE_PSEUDO_SCHEME + object.asResource().getId().getLabelString();
            }
            ICjEdgeMutable cjEdge = cjGraph.addBiEdge(subjectId, objectId);
            cjEdge.edgeType(predicateId);
        } else if (object.isLiteral()) {
            // map to CJ data property
            Literal literal = object.asLiteral();
            cjGraph.addNode(cjNode -> {
                cjNode.id(subjectId);
                cjNode.dataMutable(cjData -> cjData.add(pathOf(RdfCj.RdfInCj.rdfData, predicateId), literal.getLexicalForm()));
            });
        } else {
            throw new IllegalStateException();
        }
    }

}
