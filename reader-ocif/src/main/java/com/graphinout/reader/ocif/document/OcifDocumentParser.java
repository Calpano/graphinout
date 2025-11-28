package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.value.*;
import com.graphinout.foundation.json.value.java.JavaJsonFactory;
import com.graphinout.foundation.json.value.java.JavaJsonValues;
import com.graphinout.reader.ocif.document.extension.*;

import java.util.*;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;

/**
 * Parser that reads OCIF JSON into an {@link OcifDocument} object model.
 * <p>
 * This complements {@code com.graphinout.reader.ocif.OcifReader} (which streams into CJ)
 * by providing a convenient object-oriented API for direct manipulation.
 */
public class OcifDocumentParser {

    /** Parse a JSON string containing an OCIF document into an {@link OcifDocument}. */
    public OcifDocument parse(String json) {
        IJsonValue root = JavaJsonValues.ofJsonString(json);
        IJsonObject o = root == null ? null : root.asObject();
        OcifDocument doc = new OcifDocument();
        if (o == null) return doc;

        if (o.hasProperty(OCIF.Root.OCIF)) {
            IJsonValue v = o.get(OCIF.Root.OCIF);
            if (v != null && v.isString()) {
                doc.setOcifSchemaUri(v.asString());
            }
        }
        // optional canvas-level extensions under root.data[]
        IJsonValue canvasData = o.get(OCIF.Common.DATA);
        if (canvasData != null && canvasData.isArray()) {
            IJsonArray arr = canvasData.asArray();
            for (int i = 0; i < arr.size(); i++) {
                IJsonObject extObj = arr.get_(i).asObject();
                IOcifExtension ext = createExtensionFrom(extObj);
                if (ext != null) doc.addCanvasExtension(ext);
            }
        }

        // nodes
        IJsonValue nodesVal = o.get(OCIF.Root.NODES);
        if (nodesVal != null && nodesVal.isArray()) {
            IJsonArray nodes = nodesVal.asArray();
            for (int i = 0; i < nodes.size(); i++) {
                IJsonObject no = nodes.get_(i).asObject();
                if (no == null) continue;
                OcifNode n = new OcifNode();
                ifPresentAccept(no.get(OCIF.Common.ID), IJsonValue::asString, n::setId);
                ifPresentAccept(no.get(OCIF.Common.POSITION), this::parseNumberArray, n::setPosition);
                ifPresentAccept(no.get(OCIF.Common.SIZE), this::parseNumberArray, n::setSize);
                ifPresentAccept(no.get(OCIF.Common.RESOURCE), IJsonValue::asString, n::setResource);
                ifPresentAccept(no.get(OCIF.Common.RESOURCE_FIT), IJsonValue::asString, s -> {
                    try { n.setResourceFit(OcifNode.ResourceFit.valueOf(s)); } catch (Exception ignored) {}
                });
                // set data only if it is an array per spec; be lenient if not
                IJsonValue nodeData = no.get(OCIF.Common.DATA);
                if (nodeData != null && nodeData.isArray()) {
                    n.setData(nodeData.asArray());
                    // create typed extensions from node.data
                    IJsonArray arr = nodeData.asArray();
                    for (int j = 0; j < arr.size(); j++) {
                        IJsonObject extObj = arr.get_(j).asObject();
                        IOcifExtension ext = createExtensionFrom(extObj);
                        if (ext != null) n.addExtension(ext);
                    }
                }
                ifPresentAccept(no.get(OCIF.Common.ROTATION), IJsonValue::asNumber, d -> n.setRotation(d.doubleValue()));
                ifPresentAccept(no.get(OCIF.Common.RELATION), IJsonValue::asString, n::setRelation);
                // extras
                IJsonObjectMutable extras = JavaJsonFactory.INSTANCE.createObjectMutable();
                Set<String> known = new HashSet<>();
                known.add(OCIF.Common.ID); known.add(OCIF.Common.POSITION); known.add(OCIF.Common.SIZE); known.add(OCIF.Common.RESOURCE);
                known.add(OCIF.Common.RESOURCE_FIT); known.add(OCIF.Common.DATA); known.add(OCIF.Common.ROTATION); known.add(OCIF.Common.RELATION);
                for (String k : no.keys()) {
                    if (!known.contains(k)) {
                        extras.setProperty(k, no.get(k));
                    }
                }
                // Preserve non-array node.data in extras to keep information lossless
                if (nodeData != null && !nodeData.isArray()) {
                    extras.setProperty(OCIF.Common.DATA, nodeData);
                }
                if (!extras.isEmpty()) n.setExtras(extras);
                doc.addNode(n);
            }
        }

        // relations
        IJsonValue relsVal = o.get(OCIF.Root.RELATIONS);
        if (relsVal != null && relsVal.isArray()) {
            IJsonArray rels = relsVal.asArray();
            for (int i = 0; i < rels.size(); i++) {
                IJsonObject ro = rels.get_(i).asObject();
                if (ro == null) continue;
                OcifRelation r = new OcifRelation();
                ifPresentAccept(ro.get(OCIF.Common.ID), IJsonValue::asString, r::setId);
                // set relation.data only if it's an array; be lenient otherwise
                IJsonValue relData = ro.get(OCIF.Common.DATA);
                if (relData != null && relData.isArray()) {
                    r.setData(relData.asArray());
                    // create typed extensions from relation.data
                    IJsonArray arr = relData.asArray();
                    for (int j = 0; j < arr.size(); j++) {
                        IJsonObject extObj = arr.get_(j).asObject();
                        IOcifExtension ext = createExtensionFrom(extObj);
                        if (ext != null) r.addExtension(ext);
                    }
                }
                ifPresentAccept(ro.get(OCIF.Common.NODE), IJsonValue::asString, r::setNode);
                IJsonObjectMutable extras = JavaJsonFactory.INSTANCE.createObjectMutable();
                Set<String> known = Set.of(OCIF.Common.ID, OCIF.Common.DATA, OCIF.Common.NODE);
                for (String k : ro.keys()) {
                    if (!known.contains(k)) extras.setProperty(k, ro.get(k));
                }
                // Preserve non-array relation.data in extras
                if (relData != null && !relData.isArray()) {
                    extras.setProperty(OCIF.Common.DATA, relData);
                }
                if (!extras.isEmpty()) r.setExtras(extras);
                doc.addRelation(r);
            }
        }

        // resources
        IJsonValue resVal = o.get(OCIF.Root.RESOURCES);
        if (resVal != null && resVal.isArray()) {
            IJsonArray resArr = resVal.asArray();
            for (int i = 0; i < resArr.size(); i++) {
                IJsonObject rso = resArr.get_(i).asObject();
                if (rso == null) continue;
                OcifResource res = new OcifResource();
                ifPresentAccept(rso.get(OCIF.Common.ID), IJsonValue::asString, res::setId);
                IJsonValue repsVal = rso.get(OCIF.Resource.REPRESENTATIONS);
                if (repsVal != null && repsVal.isArray()) {
                    IJsonArray reps = repsVal.asArray();
                    for (int j = 0; j < reps.size(); j++) {
                        IJsonObject repObj = reps.get_(j).asObject();
                        if (repObj == null) continue;
                        OcifRepresentation rep = new OcifRepresentation();
                        IJsonValue lv = repObj.get(OCIF.Resource.LOCATION);
                        if (lv != null && lv.isPrimitive() && lv.asPrimitive().jsonType() == JsonType.String) {
                            rep.setLocation(lv.asString());
                        }
                        IJsonValue mt = repObj.get(OCIF.Resource.MIME_TYPE);
                        if (mt != null && mt.isPrimitive() && mt.asPrimitive().jsonType() == JsonType.String) {
                            rep.setMimeType(mt.asString());
                        }
                        IJsonValue ct = repObj.get(OCIF.Resource.CONTENT);
                        if (ct != null && ct.isPrimitive() && ct.asPrimitive().jsonType() == JsonType.String) {
                            rep.setContent(ct.asString());
                        }
                        res.addRepresentation(rep);
                    }
                }
                doc.addResource(res);
            }
        }

        // schemas
        IJsonValue schVal = o.get(OCIF.Root.SCHEMAS);
        if (schVal != null && schVal.isArray()) {
            IJsonArray schArr = schVal.asArray();
            for (int i = 0; i < schArr.size(); i++) {
                IJsonObject soj = schArr.get_(i).asObject();
                if (soj == null) continue;
                OcifSchema sch = new OcifSchema();
                ifPresentAccept(soj.get(OCIF.Schema.URI), IJsonValue::asString, sch::setUri);
                ifPresentAccept(soj.get(OCIF.Schema.SCHEMA), IJsonValue::asObject, sch::setSchema);
                ifPresentAccept(soj.get(OCIF.Schema.LOCATION), IJsonValue::asString, sch::setLocation);
                ifPresentAccept(soj.get(OCIF.Schema.NAME), IJsonValue::asString, sch::setName);
                doc.addSchema(sch);
            }
        }

        return doc;
    }

    private IOcifExtension createExtensionFrom(IJsonObject obj) {
        if (obj == null || !obj.hasProperty(OCIF.Common.TYPE)) return null;
        IJsonValue tv = obj.get(OCIF.Common.TYPE);
        if (tv == null || !tv.isPrimitive()) return null;
        String t = tv.asString();
        if (t == null) return null;
        // Canvas
        if (OcifCanvasViewportExt.TYPE.equals(t)) {
            OcifCanvasViewportExt ext = new OcifCanvasViewportExt();
            ifPresentAccept(obj.get(OCIF.Common.POSITION), IJsonValue::asArray, ext::setPosition);
            ifPresentAccept(obj.get(OCIF.Common.SIZE), IJsonValue::asArray, ext::setSize);
            ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.POSITION, OCIF.Common.SIZE)));
            return ext;
        }
        // Node extensions
        switch (t) {
            case OcifRectNodeExt.TYPE -> {
                OcifRectNodeExt ext = new OcifRectNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.STROKE_WIDTH), IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.STROKE_COLOR), IJsonValue::asString, ext::setStrokeColor);
                ifPresentAccept(obj.get(OCIF.Common.FILL_COLOR), IJsonValue::asString, ext::setFillColor);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.FILL_COLOR)));
                return ext;
            }
            case OcifOvalNodeExt.TYPE -> {
                OcifOvalNodeExt ext = new OcifOvalNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.STROKE_WIDTH), IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.STROKE_COLOR), IJsonValue::asString, ext::setStrokeColor);
                ifPresentAccept(obj.get(OCIF.Common.FILL_COLOR), IJsonValue::asString, ext::setFillColor);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.FILL_COLOR)));
                return ext;
            }
            case OcifArrowNodeExt.TYPE -> {
                OcifArrowNodeExt ext = new OcifArrowNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.STROKE_WIDTH), IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.STROKE_COLOR), IJsonValue::asString, ext::setStrokeColor);
                ifPresentAccept(obj.get(OCIF.Common.START), IJsonValue::asArray, ext::setStart);
                ifPresentAccept(obj.get(OCIF.Common.END), IJsonValue::asArray, ext::setEnd);
                ifPresentAccept(obj.get(OCIF.Common.START_MARKER), IJsonValue::asString, ext::setStartMarker);
                ifPresentAccept(obj.get(OCIF.Common.END_MARKER), IJsonValue::asString, ext::setEndMarker);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.START, OCIF.Common.END, OCIF.Common.START_MARKER, OCIF.Common.END_MARKER)));
                return ext;
            }
            case OcifPathNodeExt.TYPE -> {
                OcifPathNodeExt ext = new OcifPathNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.STROKE_WIDTH), IJsonValue::asNumber, n -> ext.setStrokeWidth(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.STROKE_COLOR), IJsonValue::asString, ext::setStrokeColor);
                ifPresentAccept(obj.get(OCIF.Common.FILL_COLOR), IJsonValue::asString, ext::setFillColor);
                ifPresentAccept(obj.get(OCIF.Common.PATH), IJsonValue::asString, ext::setPath);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.STROKE_WIDTH, OCIF.Common.STROKE_COLOR, OCIF.Common.FILL_COLOR, OCIF.Common.PATH)));
                return ext;
            }
            case OcifPortsNodeExt.TYPE -> {
                OcifPortsNodeExt ext = new OcifPortsNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.PORTS), IJsonValue::asArray, ext::setPorts);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.PORTS)));
                return ext;
            }
            case OcifNodeTransformsExt.TYPE -> {
                OcifNodeTransformsExt ext = new OcifNodeTransformsExt();
                ifPresentAccept(obj.get(OCIF.Common.SCALE), v -> v, ext::setScale);
                ifPresentAccept(obj.get(OCIF.Common.ROTATION), IJsonValue::asNumber, n -> ext.setRotation(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.ROTATION_AXIS), IJsonValue::asArray, ext::setRotationAxis);
                ifPresentAccept(obj.get(OCIF.Common.OFFSET), v -> v, ext::setOffset);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.SCALE, OCIF.Common.ROTATION, OCIF.Common.ROTATION_AXIS, OCIF.Common.OFFSET)));
                return ext;
            }
            case OcifAnchoredNodeExt.TYPE -> {
                OcifAnchoredNodeExt ext = new OcifAnchoredNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.TOP_LEFT_ANCHOR), IJsonValue::asArray, ext::setTopLeftAnchor);
                ifPresentAccept(obj.get(OCIF.Common.BOTTOM_RIGHT_ANCHOR), IJsonValue::asArray, ext::setBottomRightAnchor);
                ifPresentAccept(obj.get(OCIF.Common.TOP_LEFT_OFFSET), IJsonValue::asArray, ext::setTopLeftOffset);
                ifPresentAccept(obj.get(OCIF.Common.BOTTOM_RIGHT_OFFSET), IJsonValue::asArray, ext::setBottomRightOffset);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.TOP_LEFT_ANCHOR, OCIF.Common.BOTTOM_RIGHT_ANCHOR, OCIF.Common.TOP_LEFT_OFFSET, OCIF.Common.BOTTOM_RIGHT_OFFSET)));
                return ext;
            }
            case OcifTextStyleNodeExt.TYPE -> {
                OcifTextStyleNodeExt ext = new OcifTextStyleNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.FONT_SIZE_PX), IJsonValue::asNumber, n -> ext.setFontSizePx(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.FONT_FAMILY), IJsonValue::asString, ext::setFontFamily);
                ifPresentAccept(obj.get(OCIF.Common.COLOR), IJsonValue::asString, ext::setColor);
                ifPresentAccept(obj.get(OCIF.Common.ALIGN), IJsonValue::asString, ext::setAlign);
                ifPresentAccept(obj.get(OCIF.Common.BOLD), IJsonValue::asBoolean, ext::setBold);
                ifPresentAccept(obj.get(OCIF.Common.ITALIC), IJsonValue::asBoolean, ext::setItalic);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.FONT_SIZE_PX, OCIF.Common.FONT_FAMILY, OCIF.Common.COLOR, OCIF.Common.ALIGN, OCIF.Common.BOLD, OCIF.Common.ITALIC)));
                return ext;
            }
            case OcifThemeNodeExt.TYPE -> {
                OcifThemeNodeExt ext = new OcifThemeNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.SELECT_THEME), IJsonValue::asString, ext::setSelectTheme);
                ifPresentAccept(obj.get(OCIF.Common.THEMES), IJsonValue::asObject, ext::setThemes); // if present under a single object key; fallback handled below
                // Some files may embed theme definitions directly as properties alongside type/select-theme
                // Preserve everything else
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.SELECT_THEME, OCIF.Common.THEMES)));
                return ext;
            }
            case OcifPageNodeExt.TYPE -> {
                OcifPageNodeExt ext = new OcifPageNodeExt();
                ifPresentAccept(obj.get(OCIF.Common.PAGE_NUMBER), IJsonValue::asNumber, n -> ext.setPageNumber(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.LABEL), IJsonValue::asString, ext::setLabel);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.PAGE_NUMBER, OCIF.Common.LABEL)));
                return ext;
            }
        }
        // Relation extensions
        switch (t) {
            case OcifGroupRelationExt.TYPE -> {
                OcifGroupRelationExt ext = new OcifGroupRelationExt();
                ifPresentAccept(obj.get(OCIF.Common.MEMBERS), IJsonValue::asArray, ext::setMembers);
                ifPresentAccept(obj.get(OCIF.Common.CASCADE_DELETE), IJsonValue::asBoolean, ext::setCascadeDelete);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.MEMBERS, OCIF.Common.CASCADE_DELETE)));
                return ext;
            }
            case OcifEdgeRelationExt.TYPE -> {
                OcifEdgeRelationExt ext = new OcifEdgeRelationExt();
                ifPresentAccept(obj.get(OCIF.Common.START), IJsonValue::asString, ext::setStart);
                ifPresentAccept(obj.get(OCIF.Common.END), IJsonValue::asString, ext::setEnd);
                ifPresentAccept(obj.get(OCIF.Common.DIRECTED), IJsonValue::asBoolean, ext::setDirected);
                ifPresentAccept(obj.get(OCIF.Common.REL), IJsonValue::asString, ext::setRel);
                ifPresentAccept(obj.get(OCIF.Common.NODE), IJsonValue::asString, ext::setNode);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.START, OCIF.Common.END, OCIF.Common.DIRECTED, OCIF.Common.REL, OCIF.Common.NODE)));
                return ext;
            }
            case OcifParentChildRelationExt.TYPE -> {
                OcifParentChildRelationExt ext = new OcifParentChildRelationExt();
                ifPresentAccept(obj.get(OCIF.Common.PARENT), IJsonValue::asString, ext::setParent);
                ifPresentAccept(obj.get(OCIF.Common.CHILD), IJsonValue::asString, ext::setChild);
                ifPresentAccept(obj.get(OCIF.Common.INHERIT), IJsonValue::asBoolean, ext::setInherit);
                ifPresentAccept(obj.get(OCIF.Common.CASCADE_DELETE), IJsonValue::asBoolean, ext::setCascadeDelete);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.PARENT, OCIF.Common.CHILD, OCIF.Common.INHERIT, OCIF.Common.CASCADE_DELETE)));
                return ext;
            }
            case OcifHyperedgeRelationExt.TYPE -> {
                OcifHyperedgeRelationExt ext = new OcifHyperedgeRelationExt();
                if (obj.hasProperty(OCIF.Common.ENDPOINTS) && Objects.requireNonNull(obj.get(OCIF.Common.ENDPOINTS)).isArray()) {
                    IJsonArray eps = Objects.requireNonNull(obj.get(OCIF.Common.ENDPOINTS)).asArray();
                    for (int i = 0; i < eps.size(); i++) {
                        IJsonObject eobj = eps.get_(i).asObject();
                        if (eobj == null) continue;
                        OcifHyperedgeRelationExt.Endpoint ep = new OcifHyperedgeRelationExt.Endpoint();
                        ifPresentAccept(eobj.get(OCIF.Common.ID), IJsonValue::asString, ep::setId);
                        ifPresentAccept(eobj.get(OCIF.Common.DIRECTION), IJsonValue::asString, ep::setDirection);
                        ifPresentAccept(eobj.get(OCIF.Common.WEIGHT), IJsonValue::asNumber, n -> ep.setWeight(n.doubleValue()));
                        ep.setExtras(copyExtras(eobj, Set.of(OCIF.Common.ID, OCIF.Common.DIRECTION, OCIF.Common.WEIGHT)));
                        ext.addEndpoint(ep);
                    }
                }
                ifPresentAccept(obj.get(OCIF.Common.WEIGHT), IJsonValue::asNumber, n -> ext.setWeight(n.doubleValue()));
                ifPresentAccept(obj.get(OCIF.Common.REL), IJsonValue::asString, ext::setRel);
                ext.setExtras(copyExtras(obj, Set.of(OCIF.Common.TYPE, OCIF.Common.ENDPOINTS, OCIF.Common.WEIGHT, OCIF.Common.REL)));
                return ext;
            }
        }
        return null;
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

    private double[] parseNumberArray(IJsonValue v) {
        if (v == null || !v.isArray()) return null;
        IJsonArray a = v.asArray();
        double[] out = new double[a.size()];
        for (int i = 0; i < a.size(); i++) {
            IJsonValue iv = a.get_(i);
            Double num = null;
            if (iv.isPrimitive() && iv.asPrimitive().jsonType() == JsonType.Number) {
                num = iv.asNumber().doubleValue();
            }
            out[i] = num == null ? 0d : num;
        }
        return out;
    }
}
