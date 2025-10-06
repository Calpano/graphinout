package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.CjDirection;
import com.calpano.graphinout.base.cj.ICjEdgeType;
import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjEdgeMutable;
import com.calpano.graphinout.base.cj.element.ICjElement;
import com.calpano.graphinout.base.cj.element.ICjGraph;
import com.calpano.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.calpano.graphinout.base.cj.element.ICjGraphMutable;
import com.calpano.graphinout.base.cj.element.ICjHasDataMutable;
import com.calpano.graphinout.base.cj.element.ICjHasGraphsMutable;
import com.calpano.graphinout.base.cj.element.ICjHasIdMutable;
import com.calpano.graphinout.base.cj.element.ICjHasLabelMutable;
import com.calpano.graphinout.base.cj.element.ICjHasPortsMutable;
import com.calpano.graphinout.base.cj.element.ICjLabel;
import com.calpano.graphinout.base.cj.element.ICjNode;
import com.calpano.graphinout.base.cj.element.ICjNodeMutable;
import com.calpano.graphinout.base.cj.element.ICjPortMutable;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.element.impl.CjGraphElement;
import com.calpano.graphinout.base.cj.element.impl.CjNodeElement;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping.CjDataProperty;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement;
import com.calpano.graphinout.base.graphml.GraphmlDataType;
import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlElement;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithDesc;
import com.calpano.graphinout.base.graphml.IGraphmlElementWithId;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlNode;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.impl.GraphmlKey;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
import com.calpano.graphinout.foundation.json.value.IJsonTypedString;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonFactory;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonObject;
import com.calpano.graphinout.foundation.util.PowerStackOnClasses;
import com.calpano.graphinout.foundation.util.path.IMapLike;
import com.calpano.graphinout.foundation.util.path.KPaths;
import com.calpano.graphinout.foundation.util.path.PathResolver;
import com.calpano.graphinout.foundation.util.path.Result;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

import static com.calpano.graphinout.base.cj.CjDirection.IN;
import static com.calpano.graphinout.base.cj.CjDirection.OUT;
import static com.calpano.graphinout.base.cj.CjDirection.UNDIR;
import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.calpano.graphinout.foundation.util.Nullables.mapOrDefault;
import static com.calpano.graphinout.foundation.util.Nullables.nonNullOrDefault;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

public class Graphml2CjDocument implements GraphmlWriter {

    private static final Logger log = getLogger(Graphml2CjDocument.class);
    /** doc-level */
    private final GraphmlSchema graphmlSchema = new GraphmlSchema();
    private final PowerStackOnClasses<ICjElement> stack = PowerStackOnClasses.create();
    private CjDocumentElement cjDoc;

    private static void copyCustomAttributes(IGraphmlElement graphmlElement, ICjHasDataMutable cj) {
        // GraphML graphmlDocument attributes (k=v), like XML namespaces, become /data/cj:attributes/k
        // <graphml ATTS> ->  /data/cj:attributes/{attName}
        graphmlElement.customXmlAttributes().forEach((xmlAttName, xmlAttValue) -> //
                cj.addData(json -> //
                        json.addProperty(List.of(CjDataProperty.CustomXmlAttributes.cjPropertyKey, xmlAttName), xmlAttValue)));
    }

    /** desc goes to data: <graphml><desc> -> .data.description */
    private static void copyDesc(IGraphmlElementWithDesc graphmlElementWithDesc, ICjHasDataMutable cjWithData) {
        @Nullable IGraphmlDescription desc = graphmlElementWithDesc.desc();
        if (desc == null) return;
        cjWithData.addData(mm -> mm.addProperty(CjDataProperty.Description.cjPropertyKey, desc.value()));
    }

    public static IJsonObject toJsonValue(IGraphmlKey key, IJsonFactory factory) {
        IJsonObjectAppendable keyObject = factory.createObjectAppendable();
        String attrName = key.attrName();
        if (attrName != null) keyObject.addProperty(IGraphmlKey.ATTRIBUTE_ATTR_NAME, attrName);
        keyObject.addProperty(IGraphmlKey.ATTRIBUTE_ATTR_TYPE, key.attrType());
        keyObject.addProperty(IGraphmlKey.ATTRIBUTE_FOR, key.forType().graphmlName());
        IGraphmlDefault graphmlDefault = key.defaultValue();
        if (graphmlDefault != null) {
            keyObject.addProperty(GraphmlElements.DEFAULT, graphmlDefault.value());
        }
        IGraphmlDescription desc = key.desc();
        if (desc != null) {
            keyObject.addProperty(GraphmlElements.DESC, desc.value());
        }
        if (!key.customXmlAttributes().isEmpty()) {
            JavaJsonObject attributesObject = new JavaJsonObject();
            keyObject.addProperty("xmlAttributes", attributesObject);
            key.customXmlAttributes().forEach(attributesObject::addProperty);
        }
        return keyObject;
    }

    @Override
    public void data(IGraphmlData graphmlData) {
        ICjHasDataMutable cjHasData = stack.peek(ICjHasDataMutable.class);

        // the <data> element itself might have an id-attribute; we store it
        ifPresentAccept(graphmlData.id(), id -> //
                cjHasData.addData(json -> json.addProperty(CjDataProperty.DataId.cjPropertyKey, id)));

        String graphmlKey = graphmlData.key();
        assert graphmlKey != null;
        // process keyDefs to find mapped attName
        assert !graphmlSchema.isEmpty() : "got data " + graphmlKey + " but no keyDef for it";
        IGraphmlKey key = graphmlSchema.keyById(graphmlKey);

        if (key == null) {
            log.warn("Found no <key id=...> for <data key='" + graphmlKey + "'>. Have these keys: " + graphmlSchema.keys() + ". Assuming type string.");
            key = new GraphmlKey(null, graphmlKey, IGraphmlDescription.of("auto-created missing key"), graphmlKey, GraphmlDataType.typeString.graphmlName(), GraphmlKeyForType.All, null);
        }

        String propName = key.attrName();
        assert propName != null : "Key '" + graphmlKey + "' has no attrName?";
        String graphmlDataValue = graphmlData.value();

        // interpret GraphMl <data> and map (back to) CJ
        if (key.is(GraphmlDataElement.Label)) {
            // read CJ label encoded in GraphMl data as JSON string
            ICjLabel cjLabel = ICjLabel.fromPlainTextOrJson(graphmlDataValue);
            // attach to parent
            ICjHasLabelMutable hasLabel = (ICjHasLabelMutable) cjHasData;
            hasLabel.setLabel(label -> cjLabel.entries().forEach(cjEntry -> label.addEntry(entry -> {
                entry.language(cjEntry.language());
                entry.value(cjEntry.value());
            })));
        } else if (key.is(GraphmlDataElement.EdgeType)) {// map back to json
            ICjEdgeType edgeType = ICjEdgeType.fromJsonString(graphmlDataValue);
            assert cjHasData instanceof ICjEdgeMutable;
            ((ICjEdgeMutable) cjHasData).edgeType(edgeType);
        } else if (key.is(GraphmlDataElement.CjJsonData)) {// map back to json
            //  parse JSON
            IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(graphmlDataValue);
            cjHasData.addData(json -> json.set(jsonValue));
        } else if (key.is(GraphmlDataElement.BaseUri)) {
            // map back to native CJ baseUri
            if (cjHasData instanceof ICjDocumentChunkMutable) {
                ((ICjDocumentChunkMutable) cjHasData).baseUri(graphmlDataValue);
            } else {
                // treat as generic data
                copyData(graphmlData, key, cjHasData);
            }
        } else if (key.is(GraphmlDataElement.SyntheticNode)) {
            assert graphmlDataValue.equals("true");
            // add a marker in CJ node, so we can strip it out once the full document is constructed
            assert cjHasData instanceof ICjNodeMutable;
            cjHasData.addData(json -> json.addProperty(CjGraphmlMapping.CjDataProperty.SyntheticNode.cjPropertyKey, json.factory().createBoolean(true)));
        } else {// other, generic GraphML <data> tags
            copyData(graphmlData, key, cjHasData);
        }

    }

    @Override
    public void documentEnd() {
        this.cjDoc = stack.pop(CjDocumentElement.class);
        postProcess(this.cjDoc);
    }

    @Override
    public void documentStart(IGraphmlDocument graphmlDocument) {
        ICjDocumentChunkMutable cjDoc = stack.push(new CjDocumentElement());
        copyCustomAttributes(graphmlDocument, cjDoc);
        copyDesc(graphmlDocument, cjDoc);
    }

    @Override
    public void edgeEnd() {
        stack.pop(ICjEdgeMutable.class);
    }

    @Override
    public void edgeStart(IGraphmlEdge graphmlEdge) {
        assert graphmlEdge.isValid();
        stack.peek(ICjGraphMutable.class).addEdge(cjEdge -> {
            stack.push(cjEdge);
            copyId(graphmlEdge, cjEdge);
            copyCustomAttributes(graphmlEdge, cjEdge);
            copyDesc(graphmlEdge, cjEdge);

            boolean edgeDirected = nonNullOrDefault(graphmlEdge.directed(), //
                    // look up graphs EdgeDefault to compute edge direction
                    mapOrDefault(stack.peekSearch(ICjGraphChunkMutable.class).jsonValue(), j -> //
                                    j.resolve(CjDataProperty.EdgeDefault.cjPropertyKey), //
                            edgeDefault -> IGraphmlGraph.EdgeDefault.valueOf(edgeDefault.asString()) == IGraphmlGraph.EdgeDefault.directed, true));

            cjEdge.addEndpoint(cjEndpoint -> {
                cjEndpoint.node(graphmlEdge.source());
                cjEndpoint.direction(edgeDirected ? IN : UNDIR);
                ifPresentAccept(graphmlEdge.sourcePort(), cjEndpoint::port);
            });
            cjEdge.addEndpoint(cjEndpoint -> {
                cjEndpoint.node(graphmlEdge.target());
                cjEndpoint.direction(edgeDirected ? OUT : UNDIR);
                ifPresentAccept(graphmlEdge.targetPort(), cjEndpoint::port);
            });
        });
    }

    @Override
    public void graphEnd() {
        stack.pop(ICjGraphMutable.class);
    }

    @Override
    public void graphStart(IGraphmlGraph graphmlGraph) {
        stack.peek(ICjHasGraphsMutable.class).addGraph(cjGraph -> {
            stack.push(cjGraph);
            copyId(graphmlGraph, cjGraph);
            copyCustomAttributes(graphmlGraph, cjGraph);
            copyDesc(graphmlGraph, cjGraph);

            IGraphmlGraph.EdgeDefault edgeDefault = graphmlGraph.edgeDefault();
            if (edgeDefault != IGraphmlGraph.EdgeDefault.DEFAULT_EDGE_DEFAULT) {
                // add to "data"
                cjGraph.addData(json -> json.addProperty(CjDataProperty.EdgeDefault.cjPropertyKey, edgeDefault.graphmlString()));
            }

            if (graphmlGraph.locator() != null) {
                // FIXME ad to CJ data
                throw new IllegalArgumentException("CJ has no locator support");
            }
        });
    }

    @Override
    public void hyperEdgeEnd() {
        stack.pop(ICjEdgeMutable.class);
    }

    @Override
    public void hyperEdgeStart(IGraphmlHyperEdge graphmlEdge) {
        stack.peek(ICjGraphMutable.class).addEdge(cjEdge -> {
            stack.push(cjEdge);
            copyId(graphmlEdge, cjEdge);
            copyCustomAttributes(graphmlEdge, cjEdge);
            copyDesc(graphmlEdge, cjEdge);
            for (IGraphmlEndpoint graphmlEndpoint : graphmlEdge.endpoints()) {
                cjEdge.addEndpoint(cjEndpoint -> {
                    cjEndpoint.node(graphmlEndpoint.node());
                    GraphmlDirection gDir = graphmlEndpoint.type();
                    CjDirection cjDir = gDir == GraphmlDirection.In ? IN : gDir == GraphmlDirection.Out ? OUT : UNDIR;
                    cjEndpoint.direction(cjDir);
                    ifPresentAccept(graphmlEndpoint.port(), cjEndpoint::port);
                });
            }
        });
    }

    @Override
    public void key(IGraphmlKey key) {
        graphmlSchema.addKey(key);
        // IMPROVE could also put in doc.data somewhere
    }

    @Override
    public void nodeEnd() {
        ICjNodeMutable cjNode = stack.pop(ICjNodeMutable.class);

        // TODO apply all <default> defined for 'node', which have not been set for this node yet
        List<IGraphmlKey> defaultKeysFor = graphmlSchema.defaultKeysFor(GraphmlKeyForType.Node);
        if (defaultKeysFor.isEmpty()) return;

        cjNode.addData(magicJson -> {
            for (IGraphmlKey key : defaultKeysFor) {
                if (magicJson.isObject() && magicJson.asObject().hasProperty(key.attrName())) {
                    // dont overwrite
                    continue;
                }
                String defaultValue = requireNonNull(key.defaultValue()).value();
                // use correct JSON type for graphml type
                GraphmlDataType type = GraphmlDataType.fromString(key.attrType());
                IJsonPrimitive jsonValue = magicJson.factory().createPrimitiveFromString(type.jsonType(), defaultValue);
                magicJson.addProperty(key.attrName(), jsonValue);
            }
        });

    }

    @Override
    public void nodeStart(IGraphmlNode node) {
        stack.peek(ICjGraphMutable.class).addNode(cjNode -> {
            stack.push(cjNode);
            copyId(node, cjNode);
            copyCustomAttributes(node, cjNode);
            copyDesc(node, cjNode);
        });
    }

    @Override
    public void portEnd() {
        stack.pop(ICjPortMutable.class);
    }

    @Override
    public void portStart(IGraphmlPort port) {
        stack.peek(ICjHasPortsMutable.class).addPort(cjPort -> {
            cjPort.id(port.name());
            copyCustomAttributes(port, cjPort);
            copyDesc(port, cjPort);
            stack.push(cjPort);
        });
    }

    public CjDocumentElement resultDoc() {
        return cjDoc;
    }

    private void copyData(IGraphmlData graphmlData, IGraphmlKey key, ICjHasDataMutable cjHasData) {
        String propName = key.attrName();
        assert propName != null : "Key '" + key + "' has no attrName?";
        String graphmlDataValue = graphmlData.value();
        // TODO find best JSON type for graphmlDataValue, e.g. 'Number'
        GraphmlDataType graphmlDataType = GraphmlDataType.fromString(key.attrType());
        // need JSON factory to create primitive TODO maybe typedString xml?
        IJsonPrimitive jsonValue = JavaJsonFactory.INSTANCE.createPrimitiveFromString(graphmlDataType.jsonType(), graphmlDataValue);

        cjHasData.addData(json -> //
                json.addProperty(propName, j -> {
                    if (graphmlData.isRawXml()) {
                        j.addProperty(IJsonTypedString.TYPE, IJsonTypedString.TYPE_XML);
                        j.addProperty(IJsonTypedString.VALUE, jsonValue);
                    } else {
                        j.set(graphmlData.value());
                        json.addProperty(propName, jsonValue);
                    }
                }));

    }

    private void copyId(IGraphmlElementWithId graphmlElementWithId, ICjHasIdMutable cjHasId) {
        ofNullable(graphmlElementWithId.id()).ifPresent(cjHasId::id);
    }

    /**
     * Add {@link GraphmlSchema} as {@code <key>}. Cleanup.
     */
    private void postProcess(CjDocumentElement cjDoc) {
        // == Add keys
        cjDoc.addData(mutableJson -> {
            mutableJson.addProperty(CjDataProperty.Keys.cjPropertyKey, keysObject -> {
                graphmlSchema.forEachKey(key -> {
                    IJsonValue keyAsJsonValue = toJsonValue(key, keysObject.factory());
                    keysObject.addProperty(key.id_(), keyAsJsonValue);
                });
            });
        });

        // == Remove synthetic nodes and put their graph as direct child of parent graph
        // FIND [../graphs/nodes/*/data] WHERE [./syntheticNode = true]
        List<String> path = KPaths.of("../graphs/[$graph]/nodes/[$node]/data");
        PathResolver pathResolver = PathResolver.create();
        pathResolver.registerMap(ICjDocument.class, value -> //
                IMapLike.ofProperty("graphs", () -> value.graphs().toList()));
        pathResolver.registerMap(ICjGraph.class, value -> //
                IMapLike.ofProperty("nodes", () -> value.nodes().toList()));
        pathResolver.registerMap(ICjNode.class, value -> //
                IMapLike.ofProperty("data", value::data));

        List<Result> graph_node_data = pathResolver.resolveAll(cjDoc, path);
        List<Result> data_with_syntheticNode = graph_node_data.stream().filter(o -> {
            List<Result> list = pathResolver.resolve1(o.value(), CjDataProperty.SyntheticNode.cjPropertyKey);
            if (list.isEmpty()) return false;
            assert list.size() == 1 : "Exactly one result for 'syntheticNode' in " + o.value() + ". Got " + list.size();
            assert list.getFirst().value() instanceof IMagicMutableJsonValue;
            IMagicMutableJsonValue mm = (IMagicMutableJsonValue) list.getFirst().value();
            return mm.asPrimitive().asBoolean() == true;
        }).toList();

        for (Result data : data_with_syntheticNode) {
            // FIXME we might be nested deeper
            assert data.values().size() == 7;
            assert data.values().get(0) instanceof ICjDocument : "root is always at 0";
            assert data.values().get(1) instanceof ICjDocument : "reached via '..'  step as well";
            assert data.values().get(2) instanceof List : "array of graphs";
            assert data.values().get(3) instanceof ICjGraph : "reached via 'graphs' step";
            assert data.values().get(4) instanceof List : "array of nodes";
            assert data.values().get(5) instanceof ICjNode : "reached via 'nodes' step";
            assert data.values().get(6) instanceof ICjData : "reached via 'data' step";
            CjGraphElement graph = (CjGraphElement) data.values().get(3);
            CjNodeElement syntheticNode = (CjNodeElement) data.values().get(5);

            List<ICjGraph> subGraphs = syntheticNode.graphs().toList();
            assert subGraphs.size() == 1;
            CjGraphElement subGraph = (CjGraphElement) subGraphs.getFirst();

            graph.addGraph(subGraph);
            graph.removeNode(syntheticNode);
        }
    }


}
