package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjArrayMutable;
import com.calpano.graphinout.base.cj.element.ICjElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CjArrayElement extends CjElement implements ICjElement, ICjArrayMutable {

    protected final List<ICjElement> elements = new ArrayList<>();
    final CjType cjType;

    public CjArrayElement(CjElement parent, CjType cjType) {
        super(parent);
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
    public void fire(CjWriter cjWriter) {
        cjType.fireStart(cjWriter);
        elements.forEach(cjElement -> cjElement.fire(cjWriter));
        cjType.fireEnd(cjWriter);
    }

    @Override
    public Stream<ICjElement> stream() {
        return elements.stream().map(x -> (ICjElement) x);
    }

}
