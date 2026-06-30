package com.graphinout.reader.graphml.cj;

import com.graphinout.base.cj.data.CjMappedProperties;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjCoreElement;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDataMutable;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpointMutable;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjHasGraphsMutable;
import com.graphinout.base.cj.document.ICjHasIdMutable;
import com.graphinout.base.cj.document.ICjHasLabelMutable;
import com.graphinout.base.cj.document.ICjHasPortsMutable;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.ICjPortMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.document.impl.CjGraphElement;
import com.graphinout.base.cj.document.impl.CjNodeElement;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.jvm.kpath.KPaths;
import com.graphinout.foundation.jvm.kpath.PathResolver;
import com.graphinout.foundation.jvm.kpath.Result;
import com.graphinout.foundation.pure.collections.IMapLike;
import com.graphinout.foundation.pure.collections.PowerStackOnClasses;
import com.graphinout.foundation.pure.collections.jajson.JsonParser;
import com.graphinout.foundation.pure.input.BaseOutput;
import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonPrimitive;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.XmlFragmentString;
import com.graphinout.reader.graphml.IGraphmlWriter;
import com.graphinout.reader.graphml.cj.CjGraphmlMapping.GraphmlDataElement;
import com.graphinout.reader.graphml.elements.GraphmlDataType;
import com.graphinout.reader.graphml.elements.GraphmlDirection;
import com.graphinout.reader.graphml.elements.GraphmlKeyForType;
import com.graphinout.reader.graphml.elements.IGraphmlData;
import com.graphinout.reader.graphml.elements.IGraphmlDescription;
import com.graphinout.reader.graphml.elements.IGraphmlDocument;
import com.graphinout.reader.graphml.elements.IGraphmlEdge;
import com.graphinout.reader.graphml.elements.IGraphmlElement;
import com.graphinout.reader.graphml.elements.IGraphmlElementWithDesc;
import com.graphinout.reader.graphml.elements.IGraphmlElementWithId;
import com.graphinout.reader.graphml.elements.IGraphmlEndpoint;
import com.graphinout.reader.graphml.elements.IGraphmlGraph;
import com.graphinout.reader.graphml.elements.IGraphmlHyperEdge;
import com.graphinout.reader.graphml.elements.IGraphmlKey;
import com.graphinout.reader.graphml.elements.IGraphmlNode;
import com.graphinout.reader.graphml.elements.IGraphmlPort;
import com.graphinout.reader.graphml.elements.impl.GraphmlKey;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static com.graphinout.base.cj.data.CjDataProperty.CustomXmlAttributes;
import static com.graphinout.base.cj.data.CjDataProperty.DataId;
import static com.graphinout.base.cj.data.CjDataProperty.Description;
import static com.graphinout.base.cj.data.CjDataProperty.EdgeDefault;
import static com.graphinout.base.cj.data.CjDataProperty.Keys;
import static com.graphinout.base.cj.data.CjDataProperty.SyntheticNode;
import static com.graphinout.base.cj.document.CjDirection.IN;
import static com.graphinout.base.cj.document.CjDirection.OUT;
import static com.graphinout.base.cj.document.CjDirection.UNDIR;
import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.mapNonNull;
import static com.graphinout.foundation.pure.functional.Nullables.nonNull;
import static com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep.pathOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

public class Graphml2CjDocument extends BaseOutput implements IGraphmlWriter {

    private static final Logger log = getLogger(Graphml2CjDocument.class);
    /** doc-level */
    private final GraphmlSchema graphmlSchema = new GraphmlSchema();
    private final PowerStackOnClasses<Object> stack = PowerStackOnClasses.create();
    private CjDocumentElement cjDoc;

    private static void copyCustomAttributes(IGraphmlElement graphmlElement, ICjHasDataMutable cj) {
        // GraphML graphmlDocument attributes (k=v), like XML namespaces, become /data/graphml:xmlAttributes/k
        // <graphml ATTS> ->  /data/graphml:xmlAttributes/{attName}
        Map<String, String> xmlAttributes = graphmlElement.customXmlAttributes();

        cj.dataMutable(m -> //
                xmlAttributes.forEach((xmlAttName, xmlAttValue) -> //
                        m.add(pathOf(CustomXmlAttributes.cjPropertyKey, xmlAttName), xmlAttValue)));
    }

    /** desc goes to data: <graphml><desc> -> .data.description */
    private static void copyDesc(IGraphmlElementWithDesc graphmlElementWithDesc, ICjHasDataMutable cjWithData) {
        ifPresentAccept(graphmlElementWithDesc.desc(), desc -> //
                cjWithData.dataMutable(m -> //
                        m.add(pathOf(Description.cjPropertyKey), desc.xmlValue())));
    }


    @Override
    public void data(IGraphmlData graphmlData) {
        ICjHasDataMutable cjHasData = stack.peek(ICjHasDataMutable.class);

        // the <data> element itself might have an id-attribute; we store it
        ifPresentAccept(graphmlData.id(), id -> //
                cjHasData.dataMutable(m -> m.add(DataId.cjPropertyKey, id)));

        String graphmlKey = graphmlData.key();
        assert graphmlKey != null;
        // process keyDefs to find mapped attName
        assert !graphmlSchema.isEmpty() : "got data " + graphmlKey + " but no keyDef for it";
        IGraphmlKey key = graphmlSchema.keyById(graphmlKey);

        if (key == null) {
            sendContentError_Warn("No <key id='" + graphmlKey + "'> declared for <data key='" + graphmlKey + "'>; assuming type string");
            key = new GraphmlKey(null, graphmlKey, IGraphmlDescription.of("auto-created missing key"), graphmlKey, GraphmlDataType.typeString.graphmlName(), GraphmlKeyForType.All, null);
        }

        String propName = key.attrName();
        assert propName != null : "Key '" + graphmlKey + "' has no attrName?";
        XmlFragmentString xmlValue = graphmlData.xmlValue();
        String graphmlDataValue = xmlValue.rawXml();

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
            ICjElementType edgeType = ICjElementType.fromJsonString(graphmlDataValue);
            assert cjHasData instanceof ICjEdgeMutable;
            ((ICjEdgeMutable) cjHasData).edgeType(edgeType);
        } else if (key.is(GraphmlDataElement.NodeTypes)) {// map back to json array
            // Parse JSON array of types: ["type1", "type2", ...]
            IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(graphmlDataValue);
            assert cjHasData instanceof ICjNodeMutable;
            ICjNodeMutable node = (ICjNodeMutable) cjHasData;
            jsonValue.asArray().forEach(typeValue -> {
                ICjElementType nodeType = ICjElementType.of(typeValue.asString());
                node.addType(nodeType);
            });
        } else if (key.is(GraphmlDataElement.CjJsonData)) {// map back to json
            //  parse JSON
            IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(graphmlDataValue);
            cjHasData.dataMutable(m -> m.setJsonValue(jsonValue));
        } else if (key.is(GraphmlDataElement.Context)) {
            // map back to native CJ document @context
            if (cjHasData instanceof ICjDocumentChunkMutable) {
                // Parse JSON string back to Map<String,String>
                IJsonValue jsonValue = JsonReaderImpl.readToJsonValue(graphmlDataValue);
                if (jsonValue.isObject()) {
                    java.util.Map<String, String> context = new java.util.LinkedHashMap<>();
                    jsonValue.asObject().forEach((k2, v) -> context.put(k2, v.asString()));
                    ((ICjDocumentChunkMutable) cjHasData).context(context);
                }
            } else {
                // treat as generic data
                copyData(graphmlData, key, cjHasData);
            }
        } else if (key.is(GraphmlDataElement.SyntheticNode)) {
            assert graphmlDataValue.equals("true");
            // add a marker in the CJ node, so we can strip the node out once the full document is constructed
            assert cjHasData instanceof ICjNodeMutable : "cjData is " + cjHasData.getClass() + " but the marker should only go into nodes";
            cjHasData.dataMutable(m -> //
                    m.add(SyntheticNode.cjPropertyKey, m.factory().createBoolean(true)));
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

            boolean edgeDirected = nonNull(

                    graphmlEdge.directed(), //

                    // look up graphs EdgeDefault to compute edge direction
                    mapNonNull(stack.peekSearch(ICjGraphChunkMutable.class).jsonValue(),

                            j -> j.resolve(EdgeDefault.cjPropertyKey), //

                            edgeDefault -> IGraphmlGraph.EdgeDefault.valueOf(edgeDefault.asString()) == IGraphmlGraph.EdgeDefault.directed,

                            true));

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
                cjGraph.dataMutable(m -> //
                        m.add(EdgeDefault.cjPropertyKey, edgeDefault.graphmlString()));
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
                cjEdge.addEndpoint(cjEndpoint -> endpoint(graphmlEndpoint, cjEndpoint));
            }
        });
    }

    @Override
    public void key(IGraphmlKey key) {
        graphmlSchema.addKey(key);
    }

    @Override
    public void nodeEnd() {
        ICjNodeMutable cjNode = stack.pop(ICjNodeMutable.class);

        // FIXME !!! same for other entities

        // apply all Graphml <default> defined for 'node', ONLY which have not been set for this node yet
        List<IGraphmlKey> defaultKeysFor = graphmlSchema.defaultKeysFor(GraphmlKeyForType.Node);
        if (defaultKeysFor.isEmpty()) return;
        cjNode.dataMutable(m -> {
            for (IGraphmlKey key : defaultKeysFor) {
                if (m.hasProperty(key.attrName())) {
                    // dont overwrite
                    continue;
                }
                XmlFragmentString defaultValue = requireNonNull(key.defaultValue()).xmlValue();
                // use correct JSON type for graphml type
                GraphmlDataType type = GraphmlDataType.fromString(key.attrType());
                JsonType desiredType = type.jsonType();
                IJsonPrimitive jsonValue = m.factory().createPrimitiveFromString(desiredType, defaultValue.rawXml(), defaultValue.xmlSpace() == XML.XmlSpace.preserve);
                m.add(key.attrName(), jsonValue);
            }
        });
    }

    /*
     This node may be synthetic. For CJ export, the CJ Doc should have been post-processed to remove such nodes.
     */
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

    /**
     *
     * @param graphmlData carries the data to add
     * @param key         defines the 'schema' for this one property
     * @param cjHasData   CJ element where to set
     */
    private void copyData(IGraphmlData graphmlData, IGraphmlKey key, ICjHasDataMutable cjHasData) {
        // use Graphml <key attr.name> as CJ data JSON property key
        String propertyKey = key.attrName();
        assert propertyKey != null : "Key '" + key + "' has no attrName?";
        GraphmlDataType declaredGraphmlDataType = GraphmlDataType.fromString(key.attrType());
        assert graphmlData != null;

        cjHasData.dataMutable(m -> {
            IJsonPrimitive jsonPrimitive = CjGraphmlMapping.toJsonPrimitive(m.factory(), declaredGraphmlDataType, graphmlData.xmlValue());
            m.add(propertyKey, jsonPrimitive);
        });
    }

    private void copyId(IGraphmlElementWithId graphmlElementWithId, ICjHasIdMutable<?> cjHasId) {
        ofNullable(graphmlElementWithId.id()).ifPresent(id -> {
            cjHasId.id(id);
            // can we simplify the id using the @context?
            if (cjHasId instanceof ICjCoreElement cjCoreElement) {
                String shortId = cjCoreElement.abbreviatedUri();
                cjHasId.id(shortId);
            }
        });
    }

    private void endpoint(IGraphmlEndpoint graphmlEndpoint, ICjEndpointMutable cjEndpoint) {
        cjEndpoint.node(graphmlEndpoint.node());

        GraphmlDirection gDir = graphmlEndpoint.type();
        CjDirection cjDir = gDir == GraphmlDirection.In ? IN : gDir == GraphmlDirection.Out ? OUT : UNDIR;
        cjEndpoint.direction(cjDir);

        ifPresentAccept(graphmlEndpoint.port(), cjEndpoint::port);

        String cjDataInGraphml = graphmlEndpoint.customXmlAttributes().get(CjMappedProperties.CJ_DATA);
        ifPresentAccept(cjDataInGraphml, jsonString -> {
            IJsonValue jsonValue = JsonParser.parse(jsonString);
            if (jsonValue != null) {
                cjEndpoint.dataMutable(data -> {
                    data.setJsonValue(jsonValue);
                });
            }
        });

        String endpointType = graphmlEndpoint.customXmlAttributes().get(CjMappedProperties.CJ_ENDPOINT_TYPE);
        ifPresentAccept(endpointType, cjEndpoint::type);
    }

    /**
     * Add {@link GraphmlSchema} as {@code <key>}. Cleanup.
     */
    private void postProcess(CjDocumentElement cjDoc) {
        // == Add keys
        cjDoc.dataMutable(m -> {
            IJsonObjectMutable o = m.factory().createObjectMutable();

            // before we serialize the GraphmlSchema, remove the CJ-builtin types, which map to native CJ constructs
            graphmlSchema.removeKeyById(GraphmlDataElement.CjJsonData.toGraphmlKey().id());
            graphmlSchema.removeKeyById(GraphmlDataElement.Label.toGraphmlKey().id());
            graphmlSchema.removeKeyById(GraphmlDataElement.EdgeType.toGraphmlKey().id());
            graphmlSchema.removeKeyById(GraphmlDataElement.NodeTypes.toGraphmlKey().id());
            graphmlSchema.removeKeyById(GraphmlDataElement.EndpointType.toGraphmlKey().id());
            graphmlSchema.removeKeyById(GraphmlDataElement.Context.toGraphmlKey().id());
            graphmlSchema.removeKeyById(GraphmlDataElement.SyntheticNode.toGraphmlKey().id());
            if (graphmlSchema.isEmpty()) return;
            graphmlSchema.toJson(o);
            m.add(Keys.cjPropertyKey, o);
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
        pathResolver.registerMap(ICjDataMutable.class, value -> {
            IJsonValue jsonValue = value.jsonValue();
            if (jsonValue == null || !jsonValue.isObject()) {
                return IMapLike.EMPTY;
            }
            return IMapLike.ofMap(jsonValue.asObject().toJaJsonMap());
        });

        List<Result> graph_node_data = pathResolver.resolveAll(cjDoc, path);
        List<Result> data_with_syntheticNode = graph_node_data.stream().filter(o -> {
            List<Result> list = pathResolver.resolve1(o.value(), SyntheticNode.cjPropertyKey);
            if (list.isEmpty()) return false;
            assert list.size() == 1 : "Exactly one result for 'syntheticNode' in " + o.value() + ". Got " + list.size();
            assert list.getFirst().value() instanceof Boolean;
            return (Boolean) list.getFirst().value();
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
