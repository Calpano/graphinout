package com.graphinout.base.input;

import com.graphinout.foundation.pure.input.ContentError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ContentErrorList implements Consumer<ContentError> {

    private final List<ContentError> list = new ArrayList<>();

    public static ContentErrorList create() {
        return new ContentErrorList();
    }

    @Override
    public void accept(ContentError contentError) {
        list.add(contentError);
    }

    public void add(ContentError contentError) {
        list.add(contentError);
    }

    public ContentError getFirst() {
        return list.getFirst();
    }

    public List<ContentError> getList() {
        return list;
    }

    public boolean hasErrors() {
        return list.stream().anyMatch(ce -> ce.level == ContentError.ErrorLevel.Error);
    }

    public boolean hasWarnOrError() {
        return list.stream().anyMatch(ce -> ce.level == ContentError.ErrorLevel.Warn || ce.level == ContentError.ErrorLevel.Error);
    }


    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

}
