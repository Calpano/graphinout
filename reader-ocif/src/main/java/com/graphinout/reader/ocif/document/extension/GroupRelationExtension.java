package com.graphinout.reader.ocif.document.extension;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.OCIF;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Group Relation Extension.
 */
public class GroupRelationExtension extends OcifExtension {

    public static final String TYPE_NAME = "@ocif/rel/group";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/group-rel.json";
    /** array of string IDs */
    private IJsonArray members;
    /** default app-dependent (spec suggests ability) */
    private Boolean cascadeDelete;
    public GroupRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        GroupRelationExtension ext = new GroupRelationExtension();
        obj.getMaybeAs(OCIF.Common.MEMBERS, IJsonValue::asArray, ext::setMembers);
        obj.getMaybeAs(OCIF.Common.CASCADE_DELETE, IJsonValue::asBooleanOrNull, b -> {
            if (b != null) ext.setCascadeDelete(b);
        });
        return ext;
    }

    public Boolean cascadeDelete() {return cascadeDelete;}

    @Override
    public Set<String> definedKeys() {
        return Set.of(OCIF.Common.MEMBERS, OCIF.Common.CASCADE_DELETE);
    }

    public IJsonArray members() {return members;}

    public GroupRelationExtension setCascadeDelete(Boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
        return this;
    }

    public GroupRelationExtension setMembers(IJsonArray members) {
        this.members = members;
        return this;
    }

}
