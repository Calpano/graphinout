package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjLabel;
import com.calpano.graphinout.base.cj.element.ICjLabelEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * A CJ document
 */
public class CjLabelElement extends CjArrayElement implements ICjLabel {

    public CjLabelElement(CjElement parent) {
        super(parent, CjType.ArrayOfLabelEntries);
    }

    @Override
    public Stream<ICjLabelEntry> entries() {
        return stream().map(x -> (ICjLabelEntry) x);
    }

    public CjLabelEntryElement entry(Consumer<CjLabelEntryElement> labelEntry) {
        CjLabelEntryElement entry = new CjLabelEntryElement(this);
        labelEntry.accept(entry);
        add(entry);
        return entry;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        List<CjLabelEntryElement> list = new ArrayList<>();
        entries().forEach(e -> list.add((CjLabelEntryElement) e));

        cjWriter.list(list, CjType.ArrayOfLabelEntries, CjLabelEntryElement::fire);
    }

}
