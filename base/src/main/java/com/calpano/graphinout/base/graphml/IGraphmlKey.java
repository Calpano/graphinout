package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.gio.GioDataType;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.util.Nullables;
import com.calpano.graphinout.foundation.xml.element.XmlElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.calpano.graphinout.foundation.util.Nullables.mapOrDefault;

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
    String DEFAULT_FOR = "all";


    static GraphmlKeyBuilder builder() {
        return new GraphmlKeyBuilder();
    }

    static IGraphmlKey toGraphmlKey(XmlElement key) {
        GraphmlKeyBuilder builder = builder();
        builder.id(key.attribute(ATTRIBUTE_ID));

        Nullables.ifPresentAccept(key.attribute(ATTRIBUTE_ATTR_NAME), builder::attrName);

        builder.forType(mapOrDefault(key.attribute(ATTRIBUTE_FOR), GraphmlKeyForType::valueOf, GraphmlKeyForType.All));

        builder.attrType(mapOrDefault(key.attribute(ATTRIBUTE_ATTR_TYPE), GraphmlDataType::valueOf, GraphmlDataType.typeString));

        ifPresentAccept(key.directChildren().filter(node -> node instanceof XmlElement xmlElement && xmlElement.localName().equals(GraphmlElements.DEFAULT)).map(node -> (XmlElement) node).findFirst().orElse(null), XmlElement::contentAsXml, xml -> builder.defaultValue(IGraphmlDefault.of(xml)));

        return builder.build();
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

    default boolean definesDefaultValue() {
        return defaultValue() != null;
    }

    GraphmlKeyForType forType();

    /**
     * The special Graphml XML attributes of this {@code <data>} element are 'id' (for matching in {@code <data>},
     * 'for', 'attr.name', 'attr.type'.
     * @param name_value (name, Supplier(@Nullable value))
     */
    default void graphmlAttributes(BiConsumer<String, Supplier<String>> name_value) {
        name_value.accept(ATTRIBUTE_ID, this::id);
        name_value.accept(ATTRIBUTE_FOR, () -> forType().value);
        if (!_AUTO_COMPACT || !Objects.equals(id(), attrName())) {
            name_value.accept(ATTRIBUTE_ATTR_NAME, this::attrName);
        }
        if (!_AUTO_COMPACT || !GraphmlDataType.typeString.graphmlName.equals(attrType())) {
            name_value.accept(ATTRIBUTE_ATTR_TYPE, this::attrType);
        }
    }

     /** debugging is easier, if this is more verbose */
     boolean _AUTO_COMPACT = false;

    /**
     * the id referenced in {@link IGraphmlData#key()}.
     */
    @Override
    String id();

    default @Nonnull String id_() {
        return Objects.requireNonNull(id());
    }

    /**
     * @return true if this {@code <key>} applies to the given {@link CjGraphmlMapping.GraphmlDataElement}.
     */
    boolean is(CjGraphmlMapping.GraphmlDataElement graphmlDataElement);

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
