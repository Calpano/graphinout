package com.graphinout.reader.graphml.elements.builder;

import com.graphinout.reader.graphml.elements.IGraphmlLocator;

import javax.annotation.Nullable;

/** A builder with locator support, which has usually been created via an {@link GraphmlLocatorBuilder}. */
public interface ILocatorBuilder {

    ILocatorBuilder locator(IGraphmlLocator locator);

    @Nullable
    IGraphmlLocator locator();

}
