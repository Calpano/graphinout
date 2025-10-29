package com.graphinout.reader.graphml.elements;

import com.graphinout.reader.graphml.elements.builder.GraphmlDataBuilder;
import com.graphinout.reader.graphml.elements.builder.GraphmlKeyBuilder;
import com.graphinout.foundation.util.Nullables;
import com.graphinout.foundation.xml.XML;
import com.graphinout.foundation.xml.XmlFragmentString;
import com.graphinout.foundation.xml.document.XmlContent;
import com.graphinout.foundation.xml.document.XmlDocumentFragment;
import com.graphinout.foundation.xml.document.XmlElement;
import com.graphinout.reader.graphml.cj.CjGraphmlMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.graphinout.foundation.util.Nullables.mapOrDefault;

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
    /** debugging is easier, if this is more verbose */
    boolean _AUTO_COMPACT = false;

    static GraphmlKeyBuilder builder() {
        return new GraphmlKeyBuilder();
    }

    static IGraphmlKey toGraphmlKey(XmlElement key) {
        GraphmlKeyBuilder builder = builder();
        builder.id(key.attribute(ATTRIBUTE_ID));

        Nullables.ifPresentAccept(key.attribute(ATTRIBUTE_ATTR_NAME), builder::attrName);

        builder.forType(mapOrDefault(key.attribute(ATTRIBUTE_FOR), GraphmlKeyForType::valueOf, GraphmlKeyForType.All));

        builder.attrType(mapOrDefault(key.attribute(ATTRIBUTE_ATTR_TYPE), GraphmlDataType::valueOf, GraphmlDataType.typeString));

        // if we have a <default> use its content as defaultValue
        ifPresentAccept(key.directChildren().filter(node -> node instanceof XmlElement xmlElement && xmlElement.localName().equals(GraphmlElements.DEFAULT)), node -> (XmlElement) node, defaultXmlElement -> {
            XmlContent content = defaultXmlElement;
            // TODO obtain current XmlSpace value from key-element instead
            XML.XmlSpace xmlSpace = XML.XmlSpace.fromElement(defaultXmlElement);
            XmlFragmentString xmlFragmentString = XmlDocumentFragment.of(content, xmlSpace).toXmlFragmentString();
            IGraphmlDefault defaultValue = IGraphmlDefault.of(xmlFragmentString);
            builder.defaultValue(defaultValue);
        });

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

    default GraphmlDataType attrTypeAsGraphmlDataType() {
        return GraphmlDataType.fromGraphmlName(attrType());
    }

    @Nullable
    IGraphmlDefault defaultValue();

    default boolean definesDefaultValue() {
        return defaultValue() != null;
    }

    GraphmlKeyForType forType();

    /**
     * The special Graphml XML attributes of this {@code <data>} element are 'id' (for matching in {@code <data>},
     * 'for', 'attr.name', 'attr.type'.
     *
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


    /**
     * @param xmlFragment null results in an empty self-closing {@code <data>}.
     */
    default IGraphmlData toGraphmlData(@Nullable XmlFragmentString xmlFragment) {
        return new GraphmlDataBuilder()//
                .key(id())//
                .xmlValue(xmlFragment)//
                .build();
    }

    default IGraphmlKey withId(String keyId) {
        GraphmlKeyBuilder builder = IGraphmlKey.builder();
        builder.id(keyId);
        ifPresentAccept(attrName(), builder::attrName);
        builder.attrType(GraphmlDataType.fromGraphmlName(attrType()));
        ifPresentAccept(forType(), builder::forType);
        ifPresentAccept(defaultValue(), builder::defaultValue);
        ifPresentAccept(desc(), builder::desc);
        return builder.build();
    }

}
