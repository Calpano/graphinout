package com.graphinout.reader.ocif.document.extension.relation;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;

import static com.graphinout.reader.ocif.OCIF.Common.CASCADE_DELETE;
import static com.graphinout.reader.ocif.OCIF.Common.CHILD;
import static com.graphinout.reader.ocif.OCIF.Common.INHERIT;
import static com.graphinout.reader.ocif.OCIF.Common.PARENT;

/**
 * Parent-Child Relation Extension.
 */
public class ParentChildRelationExtension extends OcifExtension implements IOcifRelationExtension {

    public static final String TYPE_NAME = "@ocif/rel/parent-child";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/parent-child-rel.json";
    private String parent;
    private String child;
    private boolean inherit;
    private boolean cascadeDelete;


    public ParentChildRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        ParentChildRelationExtension ext = new ParentChildRelationExtension();
        obj.getIfString(PARENT, ext::parent);
        obj.getIfString(CHILD, ext::child);
        obj.getMaybeAs(INHERIT, IJsonValue::asBooleanOrNull, b -> {if (b != null) ext.inherit(b);});
        obj.getMaybeAs(CASCADE_DELETE, IJsonValue::asBooleanOrNull, b -> {
            if (b != null) ext.cascadeDelete(b);
        });
        return ext;
    }

    public ParentChildRelationExtension cascadeDelete(Boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
        return this;
    }

    public String child() {return child;}

    public ParentChildRelationExtension child(String child) {
        this.child = child;
        return this;
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(PARENT, CHILD, INHERIT, CASCADE_DELETE);
    }

    public ParentChildRelationExtension inherit(Boolean inherit) {
        this.inherit = inherit;
        return this;
    }

    public boolean isCascadeDelete() {return cascadeDelete;}

    public boolean isInherit() {return inherit;}

    public String parent() {return parent;}

    public ParentChildRelationExtension parent(String parent) {
        this.parent = parent;
        return this;
    }

    public Map<String, Object> toMap() {
        return JaJson.createMap()
                .putNonNull(TYPE, typeUri())
                .putMaybe(PARENT, parent())
                .putMaybe(CHILD, child())
                .putMaybe(INHERIT, isInherit())
                .putMaybe(CASCADE_DELETE, isCascadeDelete())
                .build();
    }


}
