package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjArrayMutable;
import com.graphinout.base.cj.document.ICjElement;

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
