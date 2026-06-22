package com.graphinout.reader.rdf;

import com.graphinout.base.cj.ConnectedJson;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.graphinout.base.cj.CjConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.graphinout.base.cj.document.CjUris.BLANK_NODE_PSEUDO_SCHEME;
import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;

public class RdfModel2CjDoc {

    private static ICjNodeMutable getOrCreateNode(ICjGraphMutable cjGraph, String nodeId, Set<String> createdNodes) {
        if (!createdNodes.contains(nodeId)) {
            createdNodes.add(nodeId);
            return cjGraph.addNode(cjNode -> cjNode.id(nodeId));
        } else {
            // Node already exists, need to add data to existing node
            return cjGraph.nodes().filter(n -> nodeId.equals(n.id())).findFirst().orElseThrow().asNode();
        }
    }

    private static void rdfModel2CjGraph(Model rdfModel, ICjDocumentMutable cjDoc, ICjGraphMutable cjGraph) {
        // Track which nodes have been explicitly created to avoid duplicates
        Set<String> createdNodes = new HashSet<>();

        // E8 — first pass: collect reifiers (the subjects that annotate a base triple) and the per-statement
        // metadata hanging off them, keyed by the annotated base triple. The reader is syntax-agnostic and
        // handles BOTH encodings produced by the writer:
        //   (a) RDF-star: a (blank) node with `r rdf:reifies << s p o >>` (Turtle/TriG/N-Triples/N-Quads);
        //   (b) classic reification: `r rdf:type rdf:Statement ; rdf:subject s ; rdf:predicate p ; rdf:object o`
        //       (RDF/XML, JSON-LD, RDF/JSON, TriX).
        // The reifier's structural triples are skipped by the main loop; its remaining predicates are metadata
        // re-attached to the matching CJ edge afterwards.
        Map<Resource, EdgeMetaKey> reifierToBaseTriple = new HashMap<>();
        Map<EdgeMetaKey, EdgeMeta> edgeMetaByTriple = new LinkedHashMap<>();
        // Structural predicates of a classic rdf:Statement reifier — not edge metadata.
        Set<Resource> classicReifiers = new HashSet<>();
        StmtIterator scan = rdfModel.listStatements();
        while (scan.hasNext()) {
            Statement stmt = scan.nextStatement();
            // (a) RDF-star reifier
            if (RDF.reifies.equals(stmt.getPredicate()) && stmt.getObject().asNode() instanceof Node_Triple nt) {
                Triple base = nt.getTriple();
                EdgeMetaKey key = baseTripleKey(cjDoc, base);
                reifierToBaseTriple.put(stmt.getSubject(), key);
                edgeMetaByTriple.computeIfAbsent(key, k -> new EdgeMeta());
            }
            // (b) classic reification: an rdf:Statement whose subject/predicate/object form the base triple
            if (RDF.type.equals(stmt.getPredicate()) && RDF.Statement.equals(stmt.getObject())) {
                Resource reifier = stmt.getSubject();
                EdgeMetaKey key = classicReifierBaseTripleKey(cjDoc, rdfModel, reifier);
                if (key != null) {
                    classicReifiers.add(reifier);
                    reifierToBaseTriple.put(reifier, key);
                    edgeMetaByTriple.computeIfAbsent(key, k -> new EdgeMeta());
                }
            }
        }

        // Iterate through all rdfTriples (triples)
        StmtIterator rdfStatements = rdfModel.listStatements();
        while (rdfStatements.hasNext()) {
            Statement stmt = rdfStatements.nextStatement();
            if (RDF.reifies.equals(stmt.getPredicate())) {
                // the `r rdf:reifies << s p o >>` link itself carries no CJ data
                continue;
            }
            // structural triples of a classic rdf:Statement reifier carry no CJ data
            if (classicReifiers.contains(stmt.getSubject()) && isReificationStructure(stmt.getPredicate())) {
                continue;
            }
            EdgeMetaKey reifiedKey = reifierToBaseTriple.get(stmt.getSubject());
            if (reifiedKey != null) {
                // a metadata triple on a reifier: `r <metaPred> <metaObj>` => edge data
                collectEdgeMeta(cjDoc, edgeMetaByTriple.get(reifiedKey), stmt);
                continue;
            }
            rdfStatementToCj(stmt, cjDoc, cjGraph, createdNodes);
        }

        // E8 — re-attach collected per-edge metadata to the matching CJ edges.
        edgeMetaByTriple.forEach((key, meta) -> attachEdgeMeta(cjGraph, key, meta));
    }

    /** Identity of a base triple as it maps to a CJ edge: source id, target id, and edge type (null for {@code cj:isRelated}). */
    private record EdgeMetaKey(String subjectId, String objectId, @org.jspecify.annotations.Nullable String edgeType) {
    }

    /** Accumulated per-edge metadata: structured props (key→value) and free-text notes, mirroring the ddot reader. */
    private static final class EdgeMeta {
        final LinkedHashMap<String, IJsonValue> props = new LinkedHashMap<>();
        final List<IJsonValue> texts = new ArrayList<>();
    }

    private static EdgeMetaKey baseTripleKey(ICjDocumentChunk cjDoc, Triple base) {
        String subjectId = nodeId(cjDoc, base.getSubject());
        String objectId = nodeId(cjDoc, base.getObject());
        String predicateUri = base.getPredicate().getURI();
        String edgeType = RdfCj.CjInRdf.IS_RELATED.equals(predicateUri) ? null : cjDoc.asId_(predicateUri);
        return new EdgeMetaKey(subjectId, objectId, edgeType);
    }

    /**
     * Build the edge key for a classic reification statement resource, reading its {@code rdf:subject},
     * {@code rdf:predicate} and {@code rdf:object}. Returns {@code null} if the reifier is incomplete or its
     * subject/object are not resources / its predicate is not a URI (then it is not a recognisable base triple).
     */
    private static @org.jspecify.annotations.Nullable EdgeMetaKey classicReifierBaseTripleKey(
            ICjDocumentChunk cjDoc, Model rdfModel, Resource reifier) {
        Statement sStmt = reifier.getProperty(RDF.subject);
        Statement pStmt = reifier.getProperty(RDF.predicate);
        Statement oStmt = reifier.getProperty(RDF.object);
        if (sStmt == null || pStmt == null || oStmt == null) return null;
        if (!sStmt.getObject().isResource() || !oStmt.getObject().isResource()
                || !pStmt.getObject().isURIResource()) return null;
        Node subject = sStmt.getObject().asResource().asNode();
        Node object = oStmt.getObject().asResource().asNode();
        Node predicate = pStmt.getObject().asResource().asNode();
        return baseTripleKey(cjDoc, Triple.create(subject, predicate, object));
    }

    /** Whether the predicate is a structural triple of a classic {@code rdf:Statement} reifier (not metadata). */
    private static boolean isReificationStructure(Property predicate) {
        return RDF.type.equals(predicate) || RDF.subject.equals(predicate)
                || RDF.predicate.equals(predicate) || RDF.object.equals(predicate);
    }

    private static String nodeId(ICjDocumentChunk cjDoc, Node node) {
        if (node.isBlank()) {
            return BLANK_NODE_PSEUDO_SCHEME + node.getBlankNodeLabel();
        }
        return cjDoc.asId_(node.getURI());
    }

    private static void collectEdgeMeta(ICjDocumentChunk cjDoc, EdgeMeta meta, Statement stmt) {
        if (!stmt.getObject().isLiteral()) return;
        Literal literal = stmt.getObject().asLiteral();
        RdfLiteral rdfLit = RdfLiteral.of(literal, cjDoc::asId_);
        IJsonValue jsonValue = rdfLit.isPlain()
                ? IJsonFactory.INSTANCE.createString(literal.getLexicalForm())
                : rdfLit.jsonObject();
        if (RdfCj.CjInRdf.HAS_NOTE.equals(stmt.getPredicate().getURI())) {
            meta.texts.add(jsonValue);
        } else {
            String key = cjDoc.asId_(stmt.getPredicate().getURI());
            meta.props.put(key, jsonValue);
        }
    }

    private static void attachEdgeMeta(ICjGraphMutable cjGraph, EdgeMetaKey key, EdgeMeta meta) {
        if (meta.props.isEmpty() && meta.texts.isEmpty()) return;
        ICjEdgeMutable edge = cjGraph.edges()
                .filter(e -> matchesBaseTriple(e, key))
                .map(e -> e.asEdge())
                .findFirst()
                .orElse(null);
        if (edge == null) return;
        IJsonFactory jf = IJsonFactory.INSTANCE;
        IJsonObjectMutable data = jf.createObjectMutable();
        if (!meta.props.isEmpty()) {
            IJsonObjectMutable props = jf.createObjectMutable();
            meta.props.forEach(props::addProperty);
            data.addProperty(RdfCj.RdfInCj.LINK_PROPS, props);
        }
        if (!meta.texts.isEmpty()) {
            if (meta.texts.size() == 1) {
                data.addProperty(RdfCj.RdfInCj.LINK_TEXT, meta.texts.get(0));
            } else {
                IJsonArrayMutable arr = jf.createArrayMutable();
                meta.texts.forEach(arr::add);
                data.addProperty(RdfCj.RdfInCj.LINK_TEXT, arr);
            }
        }
        edge.dataMutable(d -> d.setJsonValue(data));
    }

    private static boolean matchesBaseTriple(com.graphinout.base.cj.document.ICjEdge edge, EdgeMetaKey key) {
        List<com.graphinout.base.cj.document.ICjEndpoint> sources = edge.sources();
        List<com.graphinout.base.cj.document.ICjEndpoint> targets = edge.targets();
        if (sources.size() != 1 || targets.size() != 1) return false;
        if (!key.subjectId().equals(sources.getFirst().node())) return false;
        if (!key.objectId().equals(targets.getFirst().node())) return false;
        return java.util.Objects.equals(key.edgeType(), edge.type());
    }

    public static void rdfModel2cjDoc(Model rdfModel, ICjDocumentMutable cjDoc, String baseUri) {
        cjDoc.connectedJson(c -> {
            // TODO later
            c.canonical(false);
            c.versionDate(ConnectedJson.CJ_LATEST_VERSION_DATE);
            c.versionNumber(ConnectedJson.CJ_LATEST_VERSION_NUMBER);
        });
        // Collect RDF namespace prefixes into @context
        Map<String, String> contextMap = new HashMap<>(rdfModel.getNsPrefixMap());
        if (baseUri != null && !baseUri.isEmpty()) {
            contextMap.put(CjConstants.VOCAB, baseUri);
        }
        if (!contextMap.isEmpty()) {
            cjDoc.context(contextMap);
        }

        cjDoc.addGraph(cjGraph -> rdfModel2CjGraph(rdfModel, cjDoc, cjGraph));
    }

    private static void rdfStatementToCj(Statement stmt, ICjDocumentMutable cjDoc, ICjGraphMutable cjGraph, Set<String> createdNodes) {
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

        // E10 — document-level metadata: a triple about the fixed `cj:thisDocument` sentinel maps to
        // CJ document data (key = asId(predicate), value = literal / object). Repeated keys accumulate
        // into an array (JsonMaker.append), mirroring node rdf:data and the ddot `ddot.it/this` reader.
        if (RdfCj.CjInRdf.THIS_DOCUMENT.equals(subjectUri) && object.isLiteral()) {
            Literal literal = object.asLiteral();
            RdfLiteral rdfLit = RdfLiteral.of(literal, cjDoc::asId_);
            cjDoc.dataMutable(cjData -> {
                if (rdfLit.isPlain()) {
                    cjData.add(pathOf(predicateId), literal.getLexicalForm());
                } else {
                    cjData.add(pathOf(predicateId), rdfLit.jsonObject());
                }
            });
            return;
        }

        // Check if this is an rdf:type triple
        if (RDF.type.equals(predicate) && object.isResource()) {
            // rdfs:Resource is the universal built-in class (every resource is one); it carries no
            // information, so ensure the node exists but do NOT record it as a type.
            if (RDFS.Resource.equals(object.asResource())) {
                getOrCreateNode(cjGraph, subjectId, createdNodes);
                return;
            }
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
        } else if (RDFS.label.equals(predicate) && object.isLiteral()) {
            // Map (subject, rdfs:label, literal) to a CJ node with label
            Literal literal = object.asLiteral();
            String labelValue = literal.getLexicalForm();
            String language = literal.getLanguage();
            ICjNodeMutable cjNode = getOrCreateNode(cjGraph, subjectId, createdNodes);
            if (language != null && !language.isEmpty()) {
                cjNode.addLabel(labelValue, language);
            } else {
                cjNode.addLabelWithoutLanguage(labelValue);
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

            // Create the edge. cj:isRelated is the RDF encoding of an untyped link (see CjDoc2RdfModel),
            // so restore it as an untyped CJ edge (no type) for a faithful round-trip.
            ICjEdgeMutable cjEdge = cjGraph.addBiEdge(subjectId, objectId);
            if (!RdfCj.CjInRdf.IS_RELATED.equals(predicate.getURI())) {
                cjEdge.edgeType(predicateId);
            }
        } else if (object.isLiteral()) {
            // map to CJ data property
            Literal literal = object.asLiteral();
            RdfLiteral rdfLit = RdfLiteral.of(literal, cjDoc::asId_);


            // Store as plain string if no datatype/lang, otherwise as object with metadata
            ICjNodeMutable cjNode = getOrCreateNode(cjGraph, subjectId, createdNodes);
            cjNode.dataMutable(cjData -> {
                if (rdfLit.isPlain()) {
                    // Plain string literal
                    String lexicalForm = literal.getLexicalForm();
                    cjData.add(pathOf(RdfCj.RdfInCj.rdfData, predicateId), lexicalForm);
                } else {
                    // Literal with datatype or language - store as JSON object
                    cjData.add(pathOf(RdfCj.RdfInCj.rdfData, predicateId), rdfLit.jsonObject());
                }
            });
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
