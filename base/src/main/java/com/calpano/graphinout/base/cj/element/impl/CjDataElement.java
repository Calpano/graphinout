package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.element.ICjDataMutable;
import com.calpano.graphinout.foundation.json.impl.IMagicMutableJsonValue;
import com.calpano.graphinout.foundation.json.impl.MagicMutableJsonValue;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonFactory;

import javax.annotation.Nullable;

public class CjDataElement extends CjHasDataElement implements ICjDataMutable {

    private final MagicMutableJsonValue magic = new MagicMutableJsonValue(JavaJsonFactory.INSTANCE, null);

    CjDataElement(@Nullable CjHasDataElement parent) {
        super(parent);
    }

    @Override
    public CjType cjType() {
        return CjType.Data;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        if (!magic.isEmpty()) {
            cjWriter.jsonDataStart();
            magic.fire(cjWriter);
            cjWriter.jsonDataEnd();
        }
    }
 @Override
    public void jsonNode(IJsonValue jsonValue) {
        this.magic.addMerge(jsonValue);
    }

    @Nullable
    @Override
    public IMagicMutableJsonValue jsonValueMutable() {
        return magic;
    }

}
