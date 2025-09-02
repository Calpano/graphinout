package com.calpano.graphinout.base.graphml;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A read-only GraphML element.
 */
public interface IGraphmlElementWithId extends IGraphmlElement {

    /** DTD: "IMPLIED", that is not required. */
    String ATTRIBUTE_ID = "id";

    @Nullable
    String id();

    static boolean isEqual(IGraphmlElementWithId a, IGraphmlElementWithId b) {
        return IGraphmlElement.isEqual(a, b) && Objects.equals(a.id(), b.id());
    }

}
