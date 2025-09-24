package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.ICjArrayMutable;
import com.calpano.graphinout.base.cj.element.ICjElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class CjArrayElement implements ICjElement, ICjArrayMutable {

    protected final List<ICjElement> elements = new ArrayList<>();
    final CjType cjType;

    public CjArrayElement(CjType cjType) {
        assert cjType.isArray();
        this.cjType = cjType;
    }

    @Override
    public void add(ICjElement element) {
        elements.add(element);
    }

    @Override
    public CjType cjType() {
        return cjType;
    }

    @Override
    public Stream<ICjElement> stream() {
        return elements.stream().map(x -> (ICjElement) x);
    }

}
