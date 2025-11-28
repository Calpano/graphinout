package com.graphinout.reader.graphml.elements;

import org.jspecify.annotations.Nullable;
import java.util.Objects;

/**
 * A read-only GraphML element.
 */
public interface IGraphmlElementWithDesc extends IGraphmlElement {

    static boolean isEqual(IGraphmlElementWithDesc a, IGraphmlElementWithDesc b) {
        return IGraphmlElement.isEqual(a, b) && Objects.equals(a.desc(), b.desc());
    }

    @Nullable
    IGraphmlDescription desc();

}
