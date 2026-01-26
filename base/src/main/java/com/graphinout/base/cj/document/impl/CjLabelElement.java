package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjLabelEntryMutable;
import com.graphinout.base.cj.document.ICjLabelMutable;
import com.graphinout.base.cj.writer.ICjWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A CJ element, representing an ArrayOfLabelEntries.
 */
public class CjLabelElement extends CjHasDataElement implements ICjLabelMutable {

    private final List<ICjLabelEntry> labelEntries = new ArrayList<>();

    @Override
    public void addEntry(Consumer<ICjLabelEntryMutable> labelEntryConsumer) {
        // this method is also used within the CjStream2ElementsWriter to hold temporary state
        // so the labelEntry might still be empty
        CjLabelEntryElement entry = new CjLabelEntryElement();
        labelEntryConsumer.accept(entry);
        labelEntries.add(entry);
    }

    @Override
    public CjType cjType() {
        return CjType.Label;
    }

    @Override
    public Stream<ICjLabelEntry> entries() {
        return labelEntries.stream();
    }

    @Override
    public void fire(ICjWriter cjWriter, boolean sort) {
        // In v7.0.0, label is an object with an "entries" array
        // labelStart() will write the "label" key and start the object
        cjWriter.labelStart();
        List<CjLabelEntryElement> list = new ArrayList<>();
        entries().forEach(e -> list.add((CjLabelEntryElement) e));
        // listStart(ArrayOfLabelEntries) will write the "entries" key
        cjWriter.list(list, CjType.ArrayOfLabelEntries, sort, (cjLabelEntryElement, cjWriter1) -> cjLabelEntryElement.fire(cjWriter1, sort));
        cjWriter.labelEnd();
    }

}
