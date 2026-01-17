package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjLabelEntryMutable;
import com.graphinout.base.cj.document.ICjLabelMutable;
import com.graphinout.base.cj.writer.ICjWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.graphinout.base.cj.CjConstants.LABEL_ENTRIES;

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
        // In v7.0.0, label is an object with an "entries" array
        // labelStart() will write the "label" key and start the object
        // listStart(ArrayOfLabelEntries) will write the "entries" key
        cjWriter.labelStart();
        List<CjLabelEntryElement> list = new ArrayList<>();
        entries().forEach(e -> list.add((CjLabelEntryElement) e));
        cjWriter.list(list, CjType.ArrayOfLabelEntries, CjLabelEntryElement::fire);
        cjWriter.labelEnd();
    }

}
