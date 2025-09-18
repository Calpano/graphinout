package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;
import com.calpano.graphinout.foundation.json.impl.MagicMutableJsonValue;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonFactory;

import javax.annotation.Nullable;

public class CjDataElement extends CjHasDataElement implements ICjData {

    private final MagicMutableJsonValue magic = new MagicMutableJsonValue(JavaJsonFactory.INSTANCE, null);

    CjDataElement(@Nullable CjHasDataElement parent) {
        super(parent);
    }

    @Override
    public CjType cjType() {
        return CjType.Data;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        if (!magic.isEmpty()) {
            cjWriter.jsonDataStart();
            magic.fire(cjWriter);
            cjWriter.jsonDataEnd();
        }
    }

    /** set the given value as the current state */
    public void jsonNode(IJsonValue jsonValue) {
        this.magic.addMerge(jsonValue);
    }

    @Nullable
    @Override
    public IMagicMutableJsonValue jsonValueMutable() {
        return magic;
    }

}
