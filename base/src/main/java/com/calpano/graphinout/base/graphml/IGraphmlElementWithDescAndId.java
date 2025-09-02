package com.calpano.graphinout.base.graphml;

import java.util.Objects;

/**
 * A read-only GraphML element.
 */
public interface IGraphmlElementWithDescAndId extends IGraphmlElementWithDesc, IGraphmlElementWithId {

    static boolean isEqual(IGraphmlElementWithDescAndId a, IGraphmlElementWithDescAndId b) {
        return IGraphmlElement.isEqual(a, b) //
                && Objects.equals(a.id(), b.id()) //
                && Objects.equals(a.desc(), b.desc());
    }

}
