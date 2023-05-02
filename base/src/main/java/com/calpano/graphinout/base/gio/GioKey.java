package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rbaba
 * @version 0.0.1
 * @implNote A GraphML-Attribute is defined by a key element which specifies the identifier, name, type and domain of the attribute.
 * <p>
 * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all GraphML-Attributes declared in the document.
 * The purpose of the name is that applications can identify the meaning of the attribute.
 * Note that the name of the GraphML-Attribute is not used inside the document, the identifier is used for this purpose.
 * <p>
 * The type of the GraphML-Attribute can be either boolean, int, long, float, double, or string.
 * <p>
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and
 * to the whole collection of graphs described by the content of <graphml>.
 * These functions are declared by <key> elements (children of <graphml>) and defined by <data> elements.
 * Occurence: <graphml>.
 * @see GioData {@link GioData}
 */
@Data
@AllArgsConstructor
@SuperBuilder
public class GioKey extends GioElementWithDescription {

    /**
     * GraphML Type data / attribute extension
     */
    @Nullable String attributeName;
    /**
     * ID of this element. Is refered to by {@link GioData#getKey()}.
     */
    private @NonNull String id;
    /**
     * describes the domain of definition for the corresponding graph attribute.
     */
    private @NonNull GioKeyForType forType;
    /**
     * In XML, this is #PCDATA, so it may contain any mix of text and tags.
     * Theoretically, this data could also be large. But in practice, this is at most used to store icons, maybe up to a few megabytes.
     */
    private @Nullable String defaultValue;
    /**
     * GraphML Type data / attribute extension
     */
    private @Nullable GioDataType attributeType;

    public Optional<String> attributeName() {
        return Optional.ofNullable(attributeName);
    }

    public Optional<GioDataType> attributeType() {
        return Optional.ofNullable(attributeType);
    }

    public GioDataType dataType() {
        return Optional.ofNullable(attributeType).orElse(GioDataType.typeString);
    }

    public Optional<String> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public void setForType(String forType) throws IllegalArgumentException {
        this.forType = GioKeyForType.keyForType(forType);
    }

}

