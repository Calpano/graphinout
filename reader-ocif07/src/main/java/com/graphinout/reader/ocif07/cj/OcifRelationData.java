package com.graphinout.reader.ocif07.cj;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.reader.ocif07.OCIF;

/**
 * Decorate an {@link IJsonObjectMutable} for OCIF relation data stored as CJ edge data
 */
public class OcifRelationData {

    private final IJsonObjectMutable o;

    public OcifRelationData(IJsonObjectMutable o) {this.o = o;}

    public boolean isEmpty() {
        return o.isEmpty();
    }

    public IJsonObject jsonObject() {
        return o;
    }

    public void node(String node) {
        o.add(OCIF.Relation.NODE, node);
    }

    public String node() {
        return o.getString(OCIF.Relation.NODE);
    }

}
