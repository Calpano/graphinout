package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGraphmlKey extends IGraphmlElementWithDescAndId {

    String ATTRIBUTE_FOR = "for";
    String ATTRIBUTE_ATTR_NAME = "attr.name";
    String ATTRIBUTE_ATTR_TYPE = "attr.type";


    static GraphmlKeyBuilder builder() {
        return new GraphmlKeyBuilder();
    }

    /** the id referenced in {@link IGraphmlData#key()} */
    @Override
    String id();

    /** the logical attribute name */
    String attrName();

    /** one of: `boolean`, `int`, `long`, `float`, `double`, `string` */
    String attrType();

    /**
     * @param name_value (name, Supplier(@Nullable value))
     */
    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_FOR, () -> forType().value);
        name_value.accept(ATTRIBUTE_ATTR_NAME, this::attrName);
        name_value.accept(ATTRIBUTE_ATTR_TYPE, this::attrType);
    }

    @Nullable
    IGraphmlDefault defaultValue();

    GraphmlKeyForType forType();

    @Override
    default String tagName() {
        return GraphmlElements.KEY;
    }

}
