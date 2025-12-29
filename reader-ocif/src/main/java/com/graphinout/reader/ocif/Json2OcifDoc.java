package com.graphinout.reader.ocif;

import com.graphinout.base.json.JavaJsons;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentError.ErrorLevel;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import com.graphinout.reader.ocif.OCIF.Common;
import com.graphinout.reader.ocif.document.IOcifNodeMutable;
import com.graphinout.reader.ocif.document.IOcifRelation;
import com.graphinout.reader.ocif.document.IOcifRelationMutable;
import com.graphinout.reader.ocif.document.IOcifRepresentation;
import com.graphinout.reader.ocif.document.IOcifResourceMutable;
import com.graphinout.reader.ocif.document.IOcifSchema;
import com.graphinout.reader.ocif.document.extension.AnchoredNodeExtension;
import com.graphinout.reader.ocif.document.extension.ArrowNodeExtension;
import com.graphinout.reader.ocif.document.extension.CanvasViewportExtension;
import com.graphinout.reader.ocif.document.extension.DataExtension;
import com.graphinout.reader.ocif.document.extension.EdgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.GroupRelationExtension;
import com.graphinout.reader.ocif.document.extension.HyperedgeRelationExtension;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.NodeTransformsExtension;
import com.graphinout.reader.ocif.document.extension.OvalNodeExtension;
import com.graphinout.reader.ocif.document.extension.PageNodeExtension;
import com.graphinout.reader.ocif.document.extension.ParentChildRelationExtension;
import com.graphinout.reader.ocif.document.extension.PathNodeExtension;
import com.graphinout.reader.ocif.document.extension.PortsNodeExtension;
import com.graphinout.reader.ocif.document.extension.RectangleExtension;
import com.graphinout.reader.ocif.document.extension.TextStyleNodeExtension;
import com.graphinout.reader.ocif.document.extension.ThemeNodeExtension;
import com.graphinout.reader.ocif.document.impl.OcifDocument;
import com.graphinout.reader.ocif.document.impl.OcifNode;
import com.graphinout.reader.ocif.document.impl.OcifRelation;
import com.graphinout.reader.ocif.document.impl.OcifResource;
import com.graphinout.reader.ocif.document.impl.OcifSchema;
import com.graphinout.reader.ocif.document.types.OcifVector23D;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresent;
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

    static void ifPresentExpectArray(IJsonObject o, String key, Consumer<IJsonValue> arrayMemberConsumer) {
        IJsonValue v = o.get(key);
        if (v == null) return;
        if (!o.isArray()) {
            throw new ContentErrorException(ContentError.of(ContentError.ErrorLevel.Warn, "Expected array"));
        }
        IJsonArray arr = v.asArray();
        for (int i = 0; i < arr.size(); i++) {
            arrayMemberConsumer.accept(arr.get_(i));
        }
    }

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
    public static OcifDocument toOcifDocument(IJsonValue jsonValue,@NonNull Consumer<ContentError> errorHandler) throws ContentErrorException {
        if (jsonValue.isObject()) {
            IJsonObject ocifJson = jsonValue.asObject();
            Json2OcifDoc json2OcifDoc = new Json2OcifDoc();
            return json2OcifDoc.jsonObject2ocifDocument(ocifJson, errorHandler);
        } else {
            throw new ContentErrorException(ErrorLevel.Error, "Invalid OCIF: root must be a JSON object");
        }
    }

    static @NonNull IOcifExtension toOcifExtension(@NonNull IJsonObject obj, Consumer<ContentError> errorHandler) {
        String typeUri = obj.getString(Common.TYPE, msg -> ContentError.error(msg).fireTo(errorHandler));

        return switch (typeUri) {
            // ALL
            case DataExtension.TYPE_URI -> DataExtension.of(obj);
            // Canvas
            case CanvasViewportExtension.TYPE_URI -> CanvasViewportExtension.of(obj);
            // Node extensions
            case AnchoredNodeExtension.TYPE_URI -> AnchoredNodeExtension.of(obj);
            case ArrowNodeExtension.TYPE_URI -> ArrowNodeExtension.of(obj);
            case NodeTransformsExtension.TYPE_URI -> NodeTransformsExtension.of(obj);
            case OvalNodeExtension.TYPE_URI -> OvalNodeExtension.of(obj);
            case PageNodeExtension.TYPE_URI -> PageNodeExtension.of(obj);
            case PathNodeExtension.TYPE_URI -> PathNodeExtension.of(obj);
            case PortsNodeExtension.TYPE_URI -> PortsNodeExtension.of(obj);
            case RectangleExtension.TYPE_URI -> RectangleExtension.of(obj);
            case TextStyleNodeExtension.TYPE_URI -> TextStyleNodeExtension.of(obj);
            case ThemeNodeExtension.TYPE_URI -> ThemeNodeExtension.of(obj);
            // Relation extensions
            case EdgeRelationExtension.TYPE_URI -> EdgeRelationExtension.of(obj);
            case GroupRelationExtension.TYPE_URI -> GroupRelationExtension.of(obj);
            case HyperedgeRelationExtension.TYPE_URI -> HyperedgeRelationExtension.of(obj);
            case ParentChildRelationExtension.TYPE_URI -> ParentChildRelationExtension.of(obj);
            // FIXME this looses the original type value
            default -> DataExtension.of(obj);
        };
    }

    private static IOcifRelation toOcifRelation(IJsonObject o, Consumer<ContentError> errorHandler) {
        IOcifRelationMutable r = new OcifRelation();
        ifPresentAccept(o.get(Common.ID), IJsonValue::asString, r::setId);

        IJsonValue data = o.get(Common.DATA);
        if (data != null) {
            data.asArray().forEach(v -> {
                IJsonObject extObj = v.asObject();
                IOcifExtension ext = toOcifExtension(extObj, errorHandler);
                r.addExtension(ext);
            });
        }

        ifPresentAccept(o.get(Common.NODE), IJsonValue::asString, r::setNode);
        r.copyUnknown(o);
        return r;
    }

    static @NonNull IOcifResourceMutable toOcifResource(IJsonObject rso, Supplier<String> idFactory, Consumer<ContentError> errorHandler) throws ContentErrorException {
        // obtain a usable resource id
        String id = Nullables.mapOrThrow(rso.get(Common.ID), IJsonValue::asString, () -> contentWarn("OCIF resource has no id"));
        IOcifResourceMutable res = new OcifResource(id);
        IJsonValue repsVal = rso.get(OCIF.Resource.REPRESENTATIONS);
        if (repsVal != null && repsVal.isArray()) {
            IJsonArray reps = repsVal.asArray();
            for (int j = 0; j < reps.size(); j++) {
                IJsonObject repObj = reps.get_(j).asObject();
                // Either content or location must be given but not both
                String mimeType = repObj.getAsNonNullStringOrThrow(OCIF.Resource.MIME_TYPE, //
                        object -> ContentErrorException.contentError("OCIF representation has no mimeType"), //
                        value -> ContentErrorException.contentError("OCIF representation.mimeType is not a string but " + value.jsonType()));
                String location = repObj.getNullOrString(OCIF.Resource.LOCATION, //
                        value -> contentWarn("OCIF representation.location is not a string but " + value.jsonType()));
                String content = repObj.getNullOrString(OCIF.Resource.CONTENT, //
                        value -> contentWarn("OCIF representation.content is not a string but " + value.jsonType()));
                if (location == null && content == null) {
                    throw ContentErrorException.contentError("OCIF representation must have either 'location' or 'content'");
                }
                if (location != null && content != null) {
                    throw contentWarn("OCIF representation must have either 'location' or 'content' -- but not both");
                }

                IOcifRepresentation rep;
                if (content != null) {
                    rep = IOcifRepresentation.ofContent(content, mimeType);
                } else {
                    rep = IOcifRepresentation.ofLocation(location, mimeType);
                }

                res.addRepresentation(rep);
            }
        }
        return res;
    }

    private static @NonNull IOcifSchema toOcifSchema(IJsonObject soj, Consumer<ContentError> errorHandler) {
        OcifSchema sch = new OcifSchema();
        ifPresentAccept(soj.get(OCIF.Schema.URI), IJsonValue::asString, sch::setUri);
        ifPresentAccept(soj.get(OCIF.Schema.SCHEMA), IJsonValue::asObject, sch::setSchema);
        ifPresentAccept(soj.get(OCIF.Schema.LOCATION), IJsonValue::asString, sch::setLocation);
        ifPresentAccept(soj.get(OCIF.Schema.NAME), IJsonValue::asString, sch::setName);
        return sch;
    }

    public @NonNull OcifDocument jsonObject2ocifDocument(@Nullable IJsonObject o, @NonNull Consumer<ContentError> errorHandler) throws ContentErrorException {
        OcifDocument doc = new OcifDocument();
        if (o == null) return doc;

        o.getIfString(OCIF.Root.OCIF_SCHEMA_URI, doc::setOcifSchemaURI);
        if (doc.ocifSchemaURI() == null) {
            errorHandler.accept(ContentError.warn("Found no OCIF schema, using v0.6"));
            doc.setOcifSchemaURI(OCIF.OcifSchema.V0_6);
        }

        // optional canvas-level extensions under root.data[]
        ifPresentExpectArrayOfObjects(o, Common.DATA, v -> //
                ifPresent(v, w -> toOcifExtension(w, errorHandler), doc::addCanvasExtension));

        // nodes
        ifPresentExpectArrayOfObjects(o, OCIF.Root.NODES, v -> //
                ifPresent(v, w -> toOcifNode(w, errorHandler), doc::addNode));

        // relations
        ifPresentExpectArrayOfObjects(o, OCIF.Root.RELATIONS, v -> //
                ifPresent(v, w -> toOcifRelation(w, errorHandler), doc::addRelation));

        // resources
        ifPresentExpectArrayOfObjects(o, OCIF.Root.RESOURCES, v -> //
                ifPresent(v, w -> toOcifResource(w, doc::createId, errorHandler), doc::addResource));

        // schemas
        ifPresentExpectArrayOfObjects(o, OCIF.Root.SCHEMAS, v -> //
                ifPresent(v, w -> toOcifSchema(w, errorHandler), doc::addSchema));

        return doc;
    }

    /** Parse a JSON string containing an OCIF document into an {@link OcifDocument}. */
    public OcifDocument jsonString2ocifDocument(@NonNull String json, @NonNull Consumer<ContentError> errorHandler) {
        return mapOrNull(JavaJsons.ofJsonString(json), IJsonValue::asObject, o -> jsonObject2ocifDocument(o, errorHandler));
    }

    private IJsonObject copyExtras(IJsonObject obj, Set<String> known) {
        IJsonObjectMutable extras = JavaJsonFactory.INSTANCE.createObjectMutable();
        if (obj != null) {
            for (String k : obj.keys()) {
                if (!known.contains(k)) {
                    extras.setProperty(k, obj.get(k));
                }
            }
        }
        return extras.isEmpty() ? null : extras;
    }

    private IOcifNodeMutable toOcifNode(IJsonObject o, Consumer<ContentError> errorHandler) {
        IOcifNodeMutable ocifNode = new OcifNode();
        ifPresentAccept(o.get(Common.ID), IJsonValue::asString, ocifNode::setId);
        ifPresentAccept(o.get(OCIF.Node.POSITION), OcifVector23D::of, ocifNode::setPosition);
        ifPresentAccept(o.get(OCIF.Node.SIZE), OcifVector23D::of, ocifNode::setSize);
        ifPresentAccept(o.get(OCIF.Node.RESOURCE), IJsonValue::asString, ocifNode::setResource);
        ifPresentAccept(o.get(OCIF.Node.RESOURCE_FIT), IJsonValue::asString, s -> {
            ocifNode.setResourceFit(IOcifNodeMutable.ResourceFit.valueOf(s));
        });
        ifPresentAccept(o.get(Common.DATA),data->{
            data.asArray().forEach(v -> {
                IOcifExtension ext = toOcifExtension(v.asObject(), errorHandler);
                ocifNode.addExtension(ext);
            });
        });
        ifPresentAccept(o.get(OCIF.Node.ROTATION), IJsonValue::asNumber, d -> ocifNode.setRotation(d.doubleValue()));
        ifPresentAccept(o.get(OCIF.Node.RELATION), IJsonValue::asString, ocifNode::setRelation);
        return ocifNode;
    }


}
