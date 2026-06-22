package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.CjUris;
import com.graphinout.base.cj.document.ICjCoreElement;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

public class CjDoc2RdfModel {

    /** Backward-compatible entry point: encode per-edge metadata as RDF-star (the original behaviour). */
    public static void cjDoc2Model(ICjDocument cjDoc, Model rdfModel) {
        cjDoc2Model(cjDoc, rdfModel, true);
    }

    /**
     * @param rdfStar {@code true} ⇒ encode per-edge metadata (E8) as RDF-star triple terms (Turtle / TriG /
     *                N-Triples / N-Quads); {@code false} ⇒ use classic {@code rdf:Statement} reification
     *                (plain triples) so it serialises in RDF/XML, JSON-LD, RDF/JSON and TriX.
     */
    public static void cjDoc2Model(ICjDocument cjDoc, Model rdfModel, boolean rdfStar) {
        cjDocData2rdfModel(cjDoc, rdfModel);
        cjDoc.graphs().forEach(cjGraph -> cjGraph2rdfModel(cjGraph, rdfModel, rdfStar));
    }

    /**
     * E10 — document-level metadata ({@code ddot.it/this}). Emit each document-data property as a triple
     * {@code (cj:thisDocument, <key>, value)}. The subject is a fixed sentinel IRI so it round-trips
     * regardless of the document's base URI. Values are plain literals (or typed/lang literals when the
     * JSON value is a {value/datatype/language} object); arrays become one triple per member.
     */
    private static void cjDocData2rdfModel(ICjDocument cjDoc, Model rdfModel) {
        @Nullable Map<String, String> context = cjDoc.context();
        cjDoc.data(cjData -> {
            IJsonValue value = cjData.jsonValue();
            if (value == null || !value.isObject()) return;
            IJsonObject dataObject = value.asObject();
            if (dataObject.keys().isEmpty()) return;
            Resource thisDoc = rdfModel.createResource(RdfCj.CjInRdf.THIS_DOCUMENT);
            for (String key : dataObject.keys()) {
                Property predicate = rdfModel.createProperty(CjUris.expandId(context, key));
                metadataValue2rdfLiterals(context, rdfModel, dataObject.get(key), literal -> rdfModel.add(thisDoc, predicate, literal));
            }
        });
    }

    /** Backward-compatible entry point: encode per-edge metadata as RDF-star (the original behaviour). */
    public static Model cjDoc2Model(ICjDocument cjDoc) {
        return cjDoc2Model(cjDoc, true);
    }

    /** @param rdfStar see {@link #cjDoc2Model(ICjDocument, Model, boolean)}. */
    public static Model cjDoc2Model(ICjDocument cjDoc, boolean rdfStar) {
        Model model = ModelFactory.createDefaultModel();
        CjDoc2RdfModel.cjDoc2Model(cjDoc, model, rdfStar);
        return model;
    }

    /** Resolve the edge-metadata encoding for a target RDF syntax. */
    public static Model cjDoc2Model(ICjDocument cjDoc, RdfFormats.RdfSyntax syntax) {
        return cjDoc2Model(cjDoc, syntax.supportsRdfStar);
    }

    private static void cjEdge2rdfModel(@Nullable Map<String, String> context, ICjEdge cjEdge, Model rdfModel, boolean rdfStar) {
        List<ICjEndpoint> endpoints = cjEdge.endpoints().toList();
        List<ICjEndpoint> sources = cjEdge.sources();
        List<ICjEndpoint> targets = cjEdge.targets();
        List<ICjEndpoint> undirectedEndpoints = cjEdge.undirectedEndpoints();

        String edgeType = cjEdge.type() != null ? CjUris.expandId(context, cjEdge.type()) : null;

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
            Resource subject = cjNodeId2rdfResource(context, sourceEndpoint.node(), rdfModel);
            Resource object = cjNodeId2rdfResource(context, targetEndpoint.node(), rdfModel);
            Property baseProperty = rdfModel.getProperty(nonNullOrDefault(edgeType, RdfCj.CjInRdf.IS_RELATED));
            {
                rdfModel.add(subject, baseProperty, object);
            }
            // E8 — per-edge metadata: RDF-star (star-capable syntaxes) or classic reification (others).
            cjEdgeData2rdf(context, cjEdge, subject, baseProperty, object, rdfModel, rdfStar);
            ifPresentAccept(sourceEndpoint.type(), sssType -> {
                // Triple: (SSS, SSS_TYPE, TTT)
                Property property = rdfModel.getProperty(CjUris.expandId(context, sssType));
                rdfModel.add(subject, property, object);
            });
            ifPresentAccept(targetEndpoint.type(), tttType -> {
                // Triple: (SSS, TTT_TYPE, TTT)
                Property property = rdfModel.getProperty(CjUris.expandId(context, tttType));
                rdfModel.add(subject, property, object);
            });

        } else {
            // Cases: (A.in B.in), (A.out B.out) or != 2 endpoints
            // reify edge as resource and attach endpoints to it
            Resource edgeResource = cjElement2rdfResource(cjEdge, rdfModel);
            rdfModel.add(edgeResource, RDF.type, rdfModel.createResource(RdfCj.CjInRdf.CJ_EDGE));
            endpoints.forEach(ep -> {
                String pUri = RdfCj.CjInRdf.directionProperty(ep.direction());
                rdfModel.add(edgeResource, rdfModel.createProperty(pUri), cjNodeId2rdfResource(context, ep.node(), rdfModel));
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
                        predicateUri = CjUris.expandId(context, ep2.type());
                    } else if (ep1.type() != null) {
                        predicateUri = CjUris.expandId(context, ep1.type());
                    } else if (cjEdge.edgeType() != null) {
                        predicateUri = CjUris.expandId(context, cjEdge.type());
                    } else {
                        predicateUri = RdfCj.CjInRdf.IS_RELATED;
                    }
                    Resource subject = cjNodeId2rdfResource(context, ep1.node(), rdfModel);
                    Property predicate = rdfModel.createProperty(predicateUri);
                    Resource object = cjNodeId2rdfResource(context, ep2.node(), rdfModel);
                    rdfModel.add(subject, predicate, object);
                }
            }
        }
    }

    /**
     * E8 — serialise per-edge CJ {@code data} as RDF-star annotations on the base triple {@code s p o}.
     * <p>
     * Edge data follows the ddot convention: structured props under {@code ddot-it:props} (a nested
     * key→value object) and free text under {@code ddot-it:text} (a string or array of strings). Each entry
     * is attached to a fresh reifier blank node {@code r} via RDF 1.2 / RDF-star:
     * <pre>
     *   r  rdf:reifies  &lt;&lt; s p o &gt;&gt;
     *   r  &lt;key&gt;        value           # one per ddot-it:props entry
     *   r  cj:hasNote    "text"          # one per ddot-it:text entry
     * </pre>
     * The quoted triple {@code << s p o >>} is a Jena triple-term node ({@code NodeFactory.createTripleTerm}).
     * This serialises in star-capable Turtle (the syntax {@code RdfReader} uses) and reads back via the same.
     */
    private static void cjEdgeData2rdf(@Nullable Map<String, String> context, ICjEdge cjEdge,
                                       Resource subject, Property baseProperty, Resource object, Model rdfModel,
                                       boolean rdfStar) {
        cjEdge.data(cjData -> {
            IJsonValue value = cjData.jsonValue();
            if (value == null || !value.isObject()) return;
            IJsonObject dataObject = value.asObject();

            // Star-capable syntaxes: one fresh reifier bnode per annotation, referring to the triple term.
            // Other syntaxes: a single rdf:Statement resource carrying all metadata predicates as plain triples.
            Node tripleTerm = rdfStar
                    ? NodeFactory.createTripleTerm(subject.asNode(), baseProperty.asNode(), object.asNode())
                    : null;
            Resource statement = rdfStar ? null : newReificationStatement(rdfModel, subject, baseProperty, object);

            IJsonValue props = dataObject.get(RdfCj.RdfInCj.LINK_PROPS);
            if (props != null && props.isObject()) {
                IJsonObject propsObject = props.asObject();
                for (String key : propsObject.keys()) {
                    Property predicate = rdfModel.createProperty(CjUris.expandId(context, key));
                    metadataValue2rdfLiterals(context, rdfModel, propsObject.get(key), literal ->
                            addMetadata(rdfModel, rdfStar, tripleTerm, statement, predicate.asNode(), literal.asNode()));
                }
            }

            IJsonValue text = dataObject.get(RdfCj.RdfInCj.LINK_TEXT);
            if (text != null && !text.isNull()) {
                Property notePredicate = rdfModel.createProperty(RdfCj.CjInRdf.HAS_NOTE);
                metadataValue2rdfLiterals(context, rdfModel, text, literal ->
                        addMetadata(rdfModel, rdfStar, tripleTerm, statement, notePredicate.asNode(), literal.asNode()));
            }
        });
    }

    /** Dispatch a single metadata triple to either the RDF-star reifier path or the classic statement resource. */
    private static void addMetadata(Model rdfModel, boolean rdfStar, @Nullable Node tripleTerm,
                                    @Nullable Resource statement, Node metaPredicate, Node metaObject) {
        if (rdfStar) {
            addReifiedAnnotation(rdfModel, tripleTerm, metaPredicate, metaObject);
        } else {
            rdfModel.getGraph().add(Triple.create(statement.asNode(), metaPredicate, metaObject));
        }
    }

    /** Add one RDF-star annotation: a fresh reifier bnode {@code r} with {@code r rdf:reifies tripleTerm} and {@code r metaPred metaObj}. */
    private static void addReifiedAnnotation(Model rdfModel, Node tripleTerm, Node metaPredicate, Node metaObject) {
        Node reifier = NodeFactory.createBlankNode();
        rdfModel.getGraph().add(Triple.create(reifier, RDF.reifies.asNode(), tripleTerm));
        rdfModel.getGraph().add(Triple.create(reifier, metaPredicate, metaObject));
    }

    /**
     * Create a classic RDF reification statement resource (a fresh blank node) for the base triple {@code s p o}:
     * {@code r rdf:type rdf:Statement ; rdf:subject s ; rdf:predicate p ; rdf:object o}. Metadata predicates are
     * later added to {@code r}. These are plain triples and serialise in every RDF syntax.
     */
    private static Resource newReificationStatement(Model rdfModel, Resource subject, Property predicate, Resource object) {
        Resource statement = rdfModel.createResource();
        rdfModel.add(statement, RDF.type, RDF.Statement);
        rdfModel.add(statement, RDF.subject, subject);
        rdfModel.add(statement, RDF.predicate, rdfModel.createResource(predicate.getURI()));
        rdfModel.add(statement, RDF.object, object);
        return statement;
    }

    private static Resource cjElement2rdfResource(@NonNull ICjCoreElement cjCoreElement, Model rdfModel) {
        String uri = cjCoreElement.uri();
        if (uri.startsWith(CjUris.BLANK_NODE_PSEUDO_SCHEME)) {
            // extract stable blank node id
            return rdfModel.createResource(new AnonId(uri.substring(CjUris.BLANK_NODE_PSEUDO_SCHEME.length())));
        }
        // Regular node
        return rdfModel.createResource(uri);
    }

    public static void cjGraph2rdfModel(ICjGraph cjGraph, Model rdfModel) {
        cjGraph2rdfModel(cjGraph, rdfModel, true);
    }

    public static void cjGraph2rdfModel(ICjGraph cjGraph, Model rdfModel, boolean rdfStar) {
        @Nullable Map<String, String> context = cjGraph.documentContext();
        // nodes that appear as an edge endpoint already materialise in RDF via that edge
        Set<String> connectedNodeIds = new HashSet<>();
        cjGraph.edges().forEach(e -> e.endpoints().forEach(ep -> connectedNodeIds.add(ep.node())));
        cjGraph.nodes().forEach(cjNode -> cjNode2rdfModel(context, cjNode, rdfModel, connectedNodeIds));
        cjGraph.edges().forEach(cjEdge -> cjEdge2rdfModel(context, cjEdge, rdfModel, rdfStar));
    }

    private static void cjNode2rdfModel(@Nullable Map<String, String> context, ICjNode cjNode, Model rdfModel,
                                        Set<String> connectedNodeIds) {
        Resource rdfSubject = cjElement2rdfResource(cjNode, rdfModel);

        // RDF has no notion of a bare node: a node materialises only by appearing in some triple. Nodes with
        // a type, label, data, or an incident edge already do. Only a truly isolated node needs an explicit
        // existence marker — and `rdf:type rdfs:Resource` (the universal built-in class) is the least-surprising
        // one. (Emitting it for every node would be redundant noise and would round-trip as a spurious type.)
        boolean hasTypes = cjNode.types().findAny().isPresent();
        boolean hasLabel = cjNode.label() != null && cjNode.label().entries().findAny().isPresent();
        IJsonValue data = cjNode.data().jsonValue();
        boolean hasData = data != null && data.isObject() && !data.asObject().keys().isEmpty();
        boolean connected = connectedNodeIds.contains(cjNode.id());
        if (!hasTypes && !hasLabel && !hasData && !connected) {
            rdfModel.add(rdfSubject, RDF.type, RDFS.Resource);
        }

        // Add node types
        cjNode.types().forEach(cjType -> {
            Resource typeResource = rdfModel.createResource(CjUris.expandId(context, cjType.type()));
            rdfModel.add(rdfSubject, RDF.type, typeResource);
        });

        // Add node labels
        if (cjNode.label() != null) {
            cjNode.label().entries().forEach(labelEntry -> {
                String labelValue = labelEntry.value();
                String language = labelEntry.language();
                Literal labelLiteral;
                if (language != null && !language.isEmpty()) {
                    labelLiteral = rdfModel.createLiteral(labelValue, language);
                } else {
                    labelLiteral = rdfModel.createLiteral(labelValue);
                }
                rdfModel.add(rdfSubject, RDFS.label, labelLiteral);
            });
        }

        // Add node data properties
        cjNode.data(cjData -> {
            IJsonValue value = cjData.jsonValue();
            if (value != null) {
                if (value.isObject()) {
                    IJsonObject dataObject = value.asObject();
                    jsonObject2rdfModel(context, rdfSubject, dataObject, rdfModel);
                }
            }
        });
    }

    // FIXME really nullable? no
    private static Resource cjNodeId2rdfResource(@Nullable Map<String, String> context, @Nullable String cjNodeId, Model rdfModel) {
        if (cjNodeId == null) {
            // Blank node
            return rdfModel.createResource(AnonId.create());
        }
        String uri = CjUris.expandId(context, cjNodeId);
        if (uri.startsWith(CjUris.BLANK_NODE_PSEUDO_SCHEME)) {
            // extract stable blank node id
            return rdfModel.createResource(new AnonId(uri.substring(CjUris.BLANK_NODE_PSEUDO_SCHEME.length())));
        }
        // Regular node
        return rdfModel.createResource(uri);
    }

    private static ICjNode findNode(ICjDocument cjDoc, String nodeId) {
        return cjDoc.nodesAll().filter(n -> nodeId.equals(n.id())).findFirst().orElse(null);
    }

    private static void jsonObject2rdfModel(@Nullable Map<String, String> context, Resource rdfSubject, IJsonObject dataObject, Model rdfModel) {
        boolean hasRdfData = false;

        // Check if there's an "rdf:data" object containing RDF property-value pairs
        if (dataObject.hasProperty(RdfCj.RdfInCj.rdfData)) {
            IJsonValue rdfDataValue = dataObject.get(RdfCj.RdfInCj.rdfData);
            if (rdfDataValue.isObject()) {
                hasRdfData = true;
                IJsonObject rdfDataObject = rdfDataValue.asObject();
                // Extract and emit RDF triples from the "rdf:data" object
                for (String predicateId : rdfDataObject.keys()) {
                    Property predicate = rdfModel.createProperty(CjUris.expandId(context, predicateId));
                    IJsonValue value = rdfDataObject.get(predicateId);

                    toRdfLiterals(context, rdfModel, value, literal -> {
                        rdfModel.add(rdfSubject, predicate, literal);
                    });

                }
            }
        }

        // If there are no rdf:data properties, or if there are other properties besides "rdf:data", emit as JSON literal
        if (!hasRdfData || dataObject.keys().stream().anyMatch(key -> !key.equals(RdfCj.RdfInCj.rdfData))) {
            if (!hasRdfData) {
                // Only emit JSON literal if there are no rdf:data properties at all
                Property cj_rdfData = rdfModel.createProperty(RdfCj.CjInRdf.HAS_DATA);
                Literal literal = rdfModel.createTypedLiteral(dataObject.toJsonString(), RDF.dtRDFJSON);
                rdfModel.add(rdfSubject, cj_rdfData, literal);
            }
        }
    }

    /**
     * Convert a CJ <em>metadata</em> value (document data E10, or per-edge data E8) to RDF literal(s).
     * Unlike {@link #toRdfLiterals} (used for node {@code rdf:data}, where every object is a literal
     * envelope), metadata values are plain by default: a string ⇒ plain literal; an array ⇒ one literal per
     * member; a JSON object is reconstructed as a typed/lang literal only when it is a literal envelope
     * (carries a {@code value} key) and otherwise emitted as a plain JSON-string literal; other scalars ⇒
     * their JSON lexical form.
     */
    private static void metadataValue2rdfLiterals(@Nullable Map<String, String> context, Model rdfModel, IJsonValue value, Consumer<Literal> literalConsumer) {
        switch (value.jsonType()) {
            case String -> literalConsumer.accept(rdfModel.createLiteral(value.asString()));
            case Array -> value.asArray().forEach(member -> metadataValue2rdfLiterals(context, rdfModel, member, literalConsumer));
            case Object -> {
                IJsonObject obj = value.asObject();
                if (obj.hasProperty(RdfLiteral.VALUE)) {
                    // literal envelope { value, datatype?, language? }
                    RdfLiteral rdfLit = new RdfLiteral(obj.mutableCopy());
                    literalConsumer.accept(rdfLit.toRdfLiteral(rdfModel, curie -> CjUris.expandId(context, curie)));
                } else {
                    // arbitrary nested object: a plain JSON-string literal
                    literalConsumer.accept(rdfModel.createLiteral(value.toJsonString()));
                }
            }
            default -> literalConsumer.accept(rdfModel.createLiteral(value.toJsonString()));
        }
    }

    private static void toRdfLiterals(@Nullable Map<String, String> context, Model rdfModel, IJsonValue value, Consumer<Literal> literalConsumer) {
        switch (value.jsonType()) {
            case String -> // Plain string literal
                    literalConsumer.accept(rdfModel.createLiteral(value.asString()));
            case Object -> {
                // Literal with metadata (datatype/language)
                IJsonObjectMutable litObj = value.asObject().mutableCopy();
                RdfLiteral rdfLit = new RdfLiteral(litObj);
                literalConsumer.accept(rdfLit.toRdfLiteral(rdfModel, curie -> CjUris.expandId(context, curie)));
            }
            case Array -> // convert each of them
                    value.asArray().forEach(member -> toRdfLiterals(context, rdfModel, member, literalConsumer));
            default ->
                // Fall back to string representation for other types
                    literalConsumer.accept(rdfModel.createLiteral(value.toJsonString()));
        }
    }

}
