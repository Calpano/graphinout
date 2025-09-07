package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;

import java.util.ArrayList;
import java.util.List;

public class CjArrayElement extends CjElement implements ICjElement {

    final CjType cjType;
    private final List<ICjElement> elements = new ArrayList<>();

    public CjArrayElement(CjElement parent, CjType cjType) {
        super(parent);
        assert cjType.isArray();
        this.cjType = cjType;
    }

    @Override
    public CjType cjType() {
        return cjType;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjType.fireStart(cjWriter);
        elements.forEach(cjElement -> cjElement.fire(cjWriter));
        cjType.fireEnd(cjWriter);
    }

}
