package com.graphinout.base.cj.element.impl;

import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.element.ICjLabelEntry;
import com.graphinout.base.cj.element.ICjLabelEntryMutable;
import com.graphinout.base.cj.element.ICjLabelMutable;
import com.graphinout.base.cj.stream.ICjWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A CJ element, representing an ArrayOfLabelEntries.
 */
public class CjLabelElement extends CjArrayElement implements ICjLabelMutable {

    public CjLabelElement() {
        super(CjType.ArrayOfLabelEntries);
    }

    @Override
    public void addEntry(Consumer<ICjLabelEntryMutable> labelEntryConsumer) {
        // this method is also used within the CjStream2ElementsWriter to hold temporary state
        // so the labelEntry might still be empty
        CjLabelEntryElement entry = new CjLabelEntryElement();
        labelEntryConsumer.accept(entry);
        add(entry);
    }

    @Override
    public Stream<ICjLabelEntry> entries() {
        return stream().map(x -> (ICjLabelEntry) x);
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        // IMPROVE handle generics better
        List<CjLabelEntryElement> list = new ArrayList<>();
        entries().forEach(e -> list.add((CjLabelEntryElement) e));
        cjWriter.list(list, CjType.ArrayOfLabelEntries, CjLabelEntryElement::fire);
    }

}
