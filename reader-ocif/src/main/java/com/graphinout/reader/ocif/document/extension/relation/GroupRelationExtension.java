package com.graphinout.reader.ocif.document.extension.relation;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.IOcifExtension;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.reader.ocif.OCIF.Common.CASCADE_DELETE;
import static com.graphinout.reader.ocif.OCIF.Common.MEMBERS;
import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * Group Relation Extension.
 */
public class GroupRelationExtension extends OcifExtension implements IOcifRelationExtension {

    public static final String TYPE_NAME = "@ocif/rel/group";
    public static final String TYPE_URI = "https://spec.canvasprotocol.org/v0.6/extensions/group-rel.json";
    /** default app-dependent (spec suggests ability) */
    private Boolean cascadeDelete;
    /** array of string IDs */
    private List<String> members = new ArrayList<>();

    public GroupRelationExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull IOcifExtension of(@NonNull IJsonObject obj) {
        GroupRelationExtension ext = new GroupRelationExtension();
        obj.getMaybeAs(MEMBERS, IJsonValue::asArray, ext::setMembers);
        obj.getMaybeAs(CASCADE_DELETE, IJsonValue::asBooleanOrNull, b -> {
            if (b != null) ext.setCascadeDelete(b);
        });
        return ext;
    }

    public Boolean cascadeDelete() {return cascadeDelete;}

    @Override
    public Set<String> definedKeys() {
        return Set.of(MEMBERS, CASCADE_DELETE);
    }


    public List<String> members() {return members;}

    public void members(List<String> members) {this.members = members;}

    public GroupRelationExtension setCascadeDelete(Boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
        return this;
    }

    public GroupRelationExtension setMembers(IJsonArray members) {
        this.members = new ArrayList<>(members.asListOfStrings());
        return this;
    }

    public Map<String, Object> toMap() {
        return JaJson.createMap().putNonNull(TYPE, typeUri()).putMaybe(MEMBERS, members()).putNonNull(CASCADE_DELETE, cascadeDelete()).build();
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        if (!members.isEmpty()) {
            o.addArray(MEMBERS, arr -> members.forEach(arr::add));
        }
        ifPresentAccept(cascadeDelete, v -> o.setBoolean(CASCADE_DELETE, v));
        return o;
    }

}
