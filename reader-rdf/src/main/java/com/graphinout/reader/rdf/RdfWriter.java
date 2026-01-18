package com.graphinout.reader.rdf;

import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.reader.rdf.cj.RdfCj;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

public class RdfWriter implements GioWriter {

    private static final Logger log = LoggerFactory.getLogger(RdfWriter.class);

    public static void cjDoc2Model(ICjDocument cjDoc, Model rdfModel) {
        String baseUri = cjDoc.baseUri() != null ? cjDoc.baseUri() : "https://example.com/"; // Default base URI

        // Add base URI as a prefix
        rdfModel.setNsPrefix("ex", baseUri);

        // Iterate through all graphs in the CJ document
        cjDoc.graphs().forEach(cjGraph -> {
            // Process nodes
            cjGraph.nodes().forEach(cjNode -> {
                Resource subject = createRdfResource(rdfModel, cjNode.id(), cjDoc);
                rdfModel.add(subject, RDF.type, RDFS.Resource);

                // Add node types
                cjNode.types().forEach(cjType -> {
                    Resource typeResource = rdfModel.createResource(cjType.type());
                    rdfModel.add(subject, RDF.type, typeResource);
                });

                // Add node data properties
                ICjData cjData = cjNode.data();
                if (cjData != null) {
                    IJsonObject dataObject = cjData.jsonValue_().asObject();
                    dataObject.keys().forEach(key -> {
                        // Check for the special "rdf:data" prefix
                        if (key.startsWith(RdfCj.RdfInCj.rdfData + "/")) {
                            String predicateUri = key.substring(RdfCj.RdfInCj.rdfData.length() + 1);
                            Property predicate = rdfModel.createProperty(predicateUri);
                            Literal literal = rdfModel.createLiteral(dataObject.getString(key));
                            rdfModel.add(subject, predicate, literal);
                        }
                    });
                }
            });

            // Process edges
            cjGraph.edges().forEach(cjEdge -> {
                List<ICjEndpoint> endpoints = cjEdge.endpoints().toList();
                if (endpoints.size() == 2) {
                    // FIXME sort order
                    ICjEndpoint sourceEndpoint = endpoints.get(0);
                    ICjEndpoint targetEndpoint = endpoints.get(1);

                    Resource subject = createRdfResource(rdfModel, sourceEndpoint.node(), cjDoc);
                    Resource object = createRdfResource(rdfModel, targetEndpoint.node(), cjDoc);

                    String predicateUri = nonNullOrDefault( cjEdge.type(), RdfCj.CjInRdf.IS_RELATED);
                    if (predicateUri != null) {
                        Property predicate = rdfModel.createProperty(predicateUri);
                        rdfModel.add(subject, predicate, object);
                    }
                } else {
                    // TODO
                }
            });
        });
    }

    private static Resource createRdfResource(Model rdfModel, String cjNodeId, ICjDocumentChunk cjDoc) {
        String uri = cjDoc.asUri(cjNodeId);
        assert uri != null : "cjNodeId cannot be null";
        if (uri.startsWith(RdfReader.BLANK_NODE_PSEUDO_SCHEME)) {
            return rdfModel.createResource(new AnonId(cjNodeId.substring(RdfReader.BLANK_NODE_PSEUDO_SCHEME.length())));
        }
        return rdfModel.createResource(cjNodeId);
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        // Collect into CjDocument
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                write(cjDoc, outputSink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter);
    }

    @Override
    public GioFileFormat fileFormat() {
        return RdfReader.FORMAT;
    }

    public void write(ICjDocument cjDoc, OutputSink outputSink) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        cjDoc2Model(cjDoc, model);

        // add namespace declarations to model
        model.setNsPrefix("cj", RdfCj.CjInRdf.VOC);
        model.setNsPrefix("rdf", RDF.uri );
        model.setNsPrefix("rdfs", RDFS.uri );

        // Write RDF as Turtle (more readable than RDF/XML)
        StringWriter stringWriter = new StringWriter();
        model.write(stringWriter, "TURTLE");
        outputSink.write(stringWriter.toString());
    }

    private void convertCjToRdf(ICjDocument cjDoc, Model model, String baseUri) {
        // Process all graphs
        cjDoc.graphs().forEach(graph -> {
            // Create resources for all nodes first
            graph.nodes().forEach(node -> {
                Resource resource = createResourceFromNode(model, cjDoc, node, baseUri);

                // Add node types if present
                if (node.types() != null) {
                    node.types().forEach(type -> {
                        Resource typeResource = model.createResource(type.type());
                        model.add(resource, RDF.type, typeResource);
                    });
                }

                // Add node data as RDF literal (if needed)
                node.data(d -> {
                    // TODO if(d.hasProperty(RdfCj.RdfInCj.RDF_LITERAL))
                    model.add(resource, RdfCj.CjInRdf.hasDataProperty,
                            // convert to JSON literal
                            model.createTypedLiteral("foo", RDF.dtRDFJSON));
                });
            });

            // Process edges to create RDF triples
            graph.edges().forEach(edge -> {
                List<ICjEndpoint> endpoints = edge.endpoints().toList();

                if (endpoints.size() == 2) {
                    ICjEndpoint sourceEndpoint = endpoints.stream().filter(ICjEndpoint::isSource).findFirst().orElse(endpoints.get(0));

                    ICjEndpoint targetEndpoint = endpoints.stream().filter(ICjEndpoint::isTarget).findFirst().orElse(endpoints.get(1));

                    String subjectId = sourceEndpoint.node();
                    String objectId = targetEndpoint.node();

                    // Get the predicate (edge type or endpoint type)
                    String predicateUri = determinePredicateUri(edge, sourceEndpoint, targetEndpoint);

                    if (predicateUri != null) {
                        ICjNode sourceNode = findNode(cjDoc, subjectId);
                        ICjNode targetNode = findNode(cjDoc, objectId);

                        if (sourceNode != null && targetNode != null) {
                            Resource subject = createResourceFromNode(model, cjDoc, sourceNode, baseUri);
                            Property predicate = model.createProperty(predicateUri);
                            Resource object = createResourceFromNode(model, cjDoc, targetNode, baseUri);
                            model.add(subject, predicate, object);
                        }
                    }
                }
            });
        });
    }

    private Literal createLiteralFromNode(Model model, ICjNode node) {
        // TODO use a sub-object to identify RDF literals
        ICjData data = node.data();
        if (data == null) {
            return model.createLiteral(node.id());
        }

        IJsonObject o = data.jsonValue_().asObject();
        RdfLiteral rdfLiteral = new RdfLiteral(o);

        return switch (rdfLiteral.kind()) {
            case Plain -> model.createLiteral(rdfLiteral.value());
            case LanguageTagged -> model.createLiteral(rdfLiteral.value(), rdfLiteral.language());
            case DataTyped -> model.createTypedLiteral(rdfLiteral.value(), rdfLiteral.datatype());
        };
    }

    private @Nullable Resource createResourceFromNode(Model model, ICjDocumentChunk cjDoc, ICjNode node, String baseUri) {
        String nodeUri = cjDoc.asUri(node.id());
        if (nodeUri == null) {
            // leave out node
            // TODO could create synthetic URI from JSON path in document or just an int id
            return null;
        }

        // Blank node
        if (nodeUri.startsWith("_:")) {
            return model.createResource(new AnonId(nodeUri.substring(2)));
        }

        // Regular node - create URI from base + ID
        return model.createResource(baseUri + nodeUri);
    }

    private String determinePredicateUri(ICjEdge edge, ICjEndpoint sourceEndpoint, ICjEndpoint targetEndpoint) {
        // Priority: endpoint type > edge type
        if (targetEndpoint.type() != null) {
            return targetEndpoint.type();
        }
        if (sourceEndpoint.type() != null) {
            return sourceEndpoint.type();
        }
        if (edge.edgeType() != null) {
            return edge.type();
        }
        return null;
    }

    private ICjNode findNode(ICjDocument cjDoc, String nodeId) {
        return cjDoc.nodes().filter(n -> nodeId.equals(n.id())).findFirst().orElse(null);
    }

}
