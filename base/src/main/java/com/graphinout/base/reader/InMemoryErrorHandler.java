package com.graphinout.base.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InMemoryErrorHandler implements Consumer<ContentError> {

    List<ContentError> list = new ArrayList<>();

    @Override
    public void accept(ContentError contentError) {
        list.add(contentError);
    }

    public boolean hasNoErrors() {
        return list.stream().noneMatch(ce -> ce.level == ContentError.ErrorLevel.Error);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List<ContentError> list() {
        return list;
    }

}
