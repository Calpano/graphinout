package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.gio.GioDataType;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The DTD says: There are only 'for' and 'id' attributes.
 * <p>
 * The primer says: "The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique
 * among all GraphML-Attributes declared in the document. The purpose of the name is that applications can identify the
 * meaning of the attribute. Note that the name of the GraphML-Attribute is not used inside the document, the identifier
 * is used for this purpose. The type of the GraphML-Attribute can be either boolean, int, long, float, double, or
 * string. These types are defined like the corresponding types in the Java(TM)-Programming language."
 * <p>
 * The XSD says: "This group consists of the two optional attributes - attr.name (gives the name for the data function)
 * - attr.type ((declares the range of values for the data function)".
 */
public interface IGraphmlKey extends IGraphmlElementWithDescAndId {

    String ATTRIBUTE_FOR = "for";
    String ATTRIBUTE_ATTR_NAME = "attr.name";
    String ATTRIBUTE_ATTR_TYPE = "attr.type";


    static GraphmlKeyBuilder builder() {
        return new GraphmlKeyBuilder();
    }

    /**
     * the OPTIONAL logical attribute name.
     * <p>
     * Our interpretation: Default is using the 'id' attribute again.
     */
    String attrName();

    /**
     * OPTIONAL. one of: `boolean`, `int`, `long`, `float`, `double`, `string`.
     * <p>
     * Our interpretation: Default is 'string'.
     */
    String attrType();

    @Nullable
    IGraphmlDefault defaultValue();

    GraphmlKeyForType forType();

    /**
     * @param name_value (name, Supplier(@Nullable value))
     */
    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_FOR, () -> forType().value);
        if (!Objects.equals(id(), attrName())) {
            name_value.accept(ATTRIBUTE_ATTR_NAME, this::attrName);
        }
        if (!GraphmlDataType.typeString.graphmlName.equals(attrType())) {
            name_value.accept(ATTRIBUTE_ATTR_TYPE, this::attrType);
        }
    }

    /** the id referenced in {@link IGraphmlData#key()} */
    @Override
    String id();

    default @Nonnull String id_() {
        return Objects.requireNonNull(id());
    }

    @Override
    default String tagName() {
        return GraphmlElements.KEY;
    }

    default GioDataType toGioDataType() {
        return GioDataType.fromGraphmlName(attrType());
    }

    default IGraphmlData toGraphmlData(String value) {
        return new GraphmlDataBuilder()//
                .key(id())//
                .value(value)//
                .build();
    }

}
