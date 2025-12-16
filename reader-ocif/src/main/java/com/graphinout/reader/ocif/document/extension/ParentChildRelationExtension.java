package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Parent-Child Relation Extension.
 */
public class ParentChildRelationExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/rel/parent-child";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/parent-child-rel.json";
    private String parent;
    private String child;
    private Boolean inherit;
    private Boolean cascadeDelete;
    private IJsonObject extras;

    public ParentChildRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        ParentChildRelationExtension ext = new ParentChildRelationExtension();
        obj.getIfString(OCIF.Common.PARENT, ext::setParent);
        obj.getIfString(OCIF.Common.CHILD, ext::setChild);
        obj.getMaybeAs(OCIF.Common.INHERIT, IJsonValue::asBooleanOrNull, b -> {if (b != null) ext.setInherit(b);});
        obj.getMaybeAs(OCIF.Common.CASCADE_DELETE, IJsonValue::asBooleanOrNull, b -> {
            if (b != null) ext.setCascadeDelete(b);
        });
        return ext;
    }

    public Boolean cascadeDelete() {return cascadeDelete;}

    public String child() {return child;}

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.PARENT, OCIF.Common.CHILD, OCIF.Common.INHERIT, OCIF.Common.CASCADE_DELETE);
    }

    public IJsonObject extras() {return extras;}

    public Boolean inherit() {return inherit;}

    public String parent() {return parent;}

    public ParentChildRelationExtension setCascadeDelete(Boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
        return this;
    }

    public ParentChildRelationExtension setChild(String child) {
        this.child = child;
        return this;
    }

    public ParentChildRelationExtension setExtras(IJsonObject extras) {
        this.extras = extras;
        return this;
    }

    public ParentChildRelationExtension setInherit(Boolean inherit) {
        this.inherit = inherit;
        return this;
    }

    public ParentChildRelationExtension setParent(String parent) {
        this.parent = parent;
        return this;
    }

}
