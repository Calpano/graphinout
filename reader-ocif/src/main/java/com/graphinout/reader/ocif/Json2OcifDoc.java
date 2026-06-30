package com.graphinout.reader.ocif;

import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentError.ErrorLevel;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF.Common;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.IOcifRelation;
import com.graphinout.reader.ocif.document.IOcifRelationMutable;
import com.graphinout.reader.ocif.document.IOcifResource;
import com.graphinout.reader.ocif.document.IOcifSchema;
import com.graphinout.reader.ocif.document.extension.DataExtension;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.canvas.CanvasViewportExtension;
import com.graphinout.reader.ocif.document.extension.canvas.CjGraphStructureCanvasExtension;
import com.graphinout.reader.ocif.document.extension.canvas.IOcifCanvasExtension;
import com.graphinout.reader.ocif.document.extension.node.AnchoredNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.ArrowNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.IOcifNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.NodeTransformsExtension;
import com.graphinout.reader.ocif.document.extension.node.OvalNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.PageNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.PathNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.PortsNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.RectangleNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.TextStyleNodeExtension;
import com.graphinout.reader.ocif.document.extension.node.ThemeNodeExtension;
import com.graphinout.reader.ocif.document.extension.relation.CjLabelRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.EdgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.GroupRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.HyperedgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.IOcifRelationExtension;
import com.graphinout.reader.ocif.document.extension.relation.ParentChildRelationExtension;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import com.graphinout.reader.ocif.document.impl.OcifNode;
import com.graphinout.reader.ocif.document.impl.OcifRelation;
import com.graphinout.reader.ocif.document.types.OcifAngle;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;
import static com.graphinout.foundation.pure.input.ContentErrorException.contentWarn;

/**
 * Parser that reads OCIF JSON into an {@link OcifDocument} object model.
 * <p>
 * This complements {@code com.graphinout.reader.ocif.OcifReader} (which streams into CJ) by providing a convenient
 * object-oriented API for direct manipulation.
 */
public class Json2OcifDoc {

    static void ifPresentExpectArrayOfObjects(IJsonObject o, String key, Consumer<IJsonObject> arrayMemberConsumer) {
        IJsonValue v = o.get(key);
        if (v == null) return;
        if (!v.isArray()) {
            throw contentWarn("Expected '" + key + "'=array, but got " + v.jsonType());
        }
        IJsonArray arr = v.asArray();
        for (int i = 0; i < arr.size(); i++) {
            IJsonValue jsonValue = arr.get_(i);
            if (jsonValue.isObject()) {
                arrayMemberConsumer.accept(jsonValue.asObject());
            } else {
                throw contentWarn("Expected object in " + key + " array");
            }
        }
    }

    public static double[] toNumberArray(IJsonValue v, Consumer<ContentError> errorHandler) {
        if (v == null || !v.isArray()) {
            errorHandler.accept(ContentError.warn("Expected array with numbers"));
            return null;
        }
        IJsonArray a = v.asArray();
        double[] out = new double[a.size()];
        for (int i = 0; i < a.size(); i++) {
            IJsonValue iv = a.get_(i);
            if (iv.isPrimitive() && iv.asPrimitive().jsonType() == JsonType.Number) {
                out[i] = iv.asNumber().doubleValue();
            } else {
                errorHandler.accept(ContentError.warn("Expected array with numbers but found a " + v.jsonType()));
            }
        }
        return out;
    }

    /**
     * @param jsonValue
     * @param errorHandler for harmless errors
     * @return
     * @throws ContentErrorException for grave errors
     */
    public static OcifDocument toOcifDocument(IJsonValue jsonValue, @NonNull Consumer<ContentError> errorHandler) throws ContentErrorException {
        if (jsonValue.isObject()) {
            IJsonObject ocifJson = jsonValue.asObject();
            Json2OcifDoc json2OcifDoc = new Json2OcifDoc();
            return json2OcifDoc.jsonObject2ocifDocument(ocifJson, errorHandler);
        } else {
            throw new ContentErrorException(ErrorLevel.Error, "Invalid OCIF: root must be a JSON object");
        }
    }

    public static @NonNull IOcifExtension toOcifExtension(@NonNull IJsonObject obj, Consumer<ContentError> errorHandler) {
        String typeNameOrUri = obj.getString(Common.TYPE, msg -> ContentError.error(msg).fireTo(errorHandler));
        if (typeNameOrUri == null) {
            throw new ContentErrorException(ErrorLevel.Error, "Invalid OCIF: missing type URI");
        }
        // FIXME must look up schemas to resolve type names to URIs
        return switch (typeNameOrUri) {
            // ALL
            case DataExtension.TYPE_URI, DataExtension.TYPE_NAME -> DataExtension.of(obj);
            // Canvas
            case CanvasViewportExtension.TYPE_URI, CanvasViewportExtension.TYPE_NAME -> CanvasViewportExtension.of(obj);
            case CjGraphStructureCanvasExtension.TYPE_URI, CjGraphStructureCanvasExtension.TYPE_NAME ->
                    CjGraphStructureCanvasExtension.of(obj);
            // Node extensions
            case AnchoredNodeExtension.TYPE_URI, AnchoredNodeExtension.TYPE_NAME -> AnchoredNodeExtension.of(obj);
            case ArrowNodeExtension.TYPE_URI, ArrowNodeExtension.TYPE_NAME -> ArrowNodeExtension.of(obj);
            case NodeTransformsExtension.TYPE_URI, NodeTransformsExtension.TYPE_NAME -> NodeTransformsExtension.of(obj);
            case OvalNodeExtension.TYPE_URI, OvalNodeExtension.TYPE_NAME -> OvalNodeExtension.of(obj);
            case PageNodeExtension.TYPE_URI, PageNodeExtension.TYPE_NAME -> PageNodeExtension.of(obj);
            case PathNodeExtension.TYPE_URI, PathNodeExtension.TYPE_NAME -> PathNodeExtension.of(obj);
            case PortsNodeExtension.TYPE_URI, PortsNodeExtension.TYPE_NAME -> PortsNodeExtension.of(obj);
            case RectangleNodeExtension.TYPE_URI, RectangleNodeExtension.TYPE_NAME -> RectangleNodeExtension.of(obj);
            case TextStyleNodeExtension.TYPE_URI, TextStyleNodeExtension.TYPE_NAME -> TextStyleNodeExtension.of(obj);
            case ThemeNodeExtension.TYPE_URI, ThemeNodeExtension.TYPE_NAME -> ThemeNodeExtension.of(obj);
            // Relation extensions
            case EdgeRelationExtension.TYPE_URI, EdgeRelationExtension.TYPE_NAME -> EdgeRelationExtension.of(obj);
            case GroupRelationExtension.TYPE_URI, GroupRelationExtension.TYPE_NAME -> GroupRelationExtension.of(obj);
            case HyperedgeRelationExtension.TYPE_URI, HyperedgeRelationExtension.TYPE_NAME ->
                    HyperedgeRelationExtension.of(obj);
            case ParentChildRelationExtension.TYPE_URI, ParentChildRelationExtension.TYPE_NAME ->
                    ParentChildRelationExtension.of(obj);
            case CjLabelRelationExtension.TYPE_URI, CjLabelRelationExtension.TYPE_NAME ->
                    CjLabelRelationExtension.of(obj);

            // FIXME this looses the original type value
            default -> DataExtension.of(obj);
        };
    }

    public static IOcifNodeMutable toOcifNode(IJsonObject o, Consumer<ContentError> errorHandler) {
        IOcifNodeMutable ocifNode = new OcifNode();
        ifPresentAccept(o.get(Common.ID), IJsonValue::asString, ocifNode::id);
        ifPresentAccept(o.get(OCIF.Node.POSITION), OcifVector23D::of, ocifNode::position);
        ifPresentAccept(o.get(OCIF.Node.SIZE), OcifVector23D::of, ocifNode::size);
        ifPresentAccept(o.get(OCIF.Node.RESOURCE), IJsonValue::asString, ocifNode::resource);
        ifPresentAccept(o.get(OCIF.Node.RESOURCE_FIT), IJsonValue::asString, s -> //
                ocifNode.resourceFit(IOcifNodeMutable.ResourceFit.valueOf(s)));
        ifPresentAccept(o.get(Common.DATA), data -> {
            data.asArray().forEach(v -> {
                IOcifExtension ext = toOcifExtension(v.asObject(), errorHandler);
                ocifNode.addNodeExtension((IOcifNodeExtension) ext);
            });
        });
        ifPresentAccept(o.get(OCIF.Node.ROTATION), OcifAngle::of, ocifNode::rotation);
        ifPresentAccept(o.get(OCIF.Node.RELATION), IJsonValue::asString, ocifNode::relation);
        return ocifNode;
    }

    private static IOcifRelation toOcifRelation(IJsonObject o, Consumer<ContentError> errorHandler) {
        IOcifRelationMutable r = new OcifRelation();
        ifPresentAccept(o.get(Common.ID), IJsonValue::asString, r::id);

        IJsonValue data = o.get(Common.DATA);
        if (data != null) {
            data.asArray().forEach(v -> {
                IJsonObject extObj = v.asObject();
                IOcifExtension ext = toOcifExtension(extObj, errorHandler);
                r.addExtension((IOcifRelationExtension) ext);
            });
        }

        ifPresentAccept(o.get(OCIF.Relation.NODE), IJsonValue::asString, r::node);
        r.copyUnknown(o);
        return r;
    }

    public @NonNull OcifDocument jsonObject2ocifDocument(@Nullable IJsonObject o, @NonNull Consumer<ContentError> errorHandler) throws ContentErrorException {
        OcifDocument doc = new OcifDocument();
        if (o == null) return doc;

        IJsonValue schemaUri = o.get(OCIF.Root.OCIF_SCHEMA_URI);
        if (schemaUri != null) {
            doc.ocifSchemaURI(schemaUri.asString());
        } else {
            errorHandler.accept(ContentError.warn("Found no OCIF schema, assuming " + OCIF.OcifSchema.DEFAULT));
            doc.ocifSchemaURI(OCIF.OcifSchema.DEFAULT);
        }

        // optional canvas-level extensions under root.data[]
        ifPresentExpectArrayOfObjects(o, Common.DATA, v -> //
                ifPresentAccept(v, w -> toOcifExtension(w, errorHandler), ext -> doc.addCanvasExtension((IOcifCanvasExtension) ext)));

        // nodes
        ifPresentExpectArrayOfObjects(o, OCIF.Root.NODES, v -> //
                ifPresentAccept(v, w -> toOcifNode(w, errorHandler), doc::addNode));

        // relations
        ifPresentExpectArrayOfObjects(o, OCIF.Root.RELATIONS, v -> //
                ifPresentAccept(v, w -> toOcifRelation(w, errorHandler), doc::addRelation));

        // resources
        ifPresentExpectArrayOfObjects(o, OCIF.Root.RESOURCES, v -> //
                ifPresentAccept(v, w -> IOcifResource.jsonToOcifResource(w, errorHandler), doc::addResource));

        // schemas
        ifPresentExpectArrayOfObjects(o, OCIF.Root.SCHEMAS, v -> //
                ifPresentAccept(v, w -> IOcifSchema.toOcifSchema(w, errorHandler), doc::addSchema));

        return doc;
    }

    /** Parse a JSON string containing an OCIF document into an {@link OcifDocument}. */
    public OcifDocument jsonString2ocifDocument(@NonNull String json, @NonNull Consumer<ContentError> errorHandler) {
        return mapOrNull(JavaJsons.ofJsonString(json), IJsonValue::asObject, o -> jsonObject2ocifDocument(o, errorHandler));
    }



}
