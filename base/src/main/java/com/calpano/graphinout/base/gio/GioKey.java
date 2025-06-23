package com.calpano.graphinout.base.gio;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rbaba

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
public class GioKey extends GioElementWithDescription {

    /**
     * GraphML Type data / attribute extension
     */
    @Nullable String attributeName;
    /**
     * ID of this element. Is refered to by {@link GioData#getKey()}.
     */
    private String id;
    /**
     * describes the domain of definition for the corresponding graph attribute.
     */
    private GioKeyForType forType;
    /**
     * In XML, this is #PCDATA, so it may contain any mix of text and tags.
     * Theoretically, this data could also be large. But in practice, this is at most used to store icons, maybe up to a few megabytes.
     */
    private @Nullable String defaultValue;
    /**
     * GraphML Type data / attribute extension
     */
    private @Nullable GioDataType attributeType;

    // Constructors
    public GioKey() {
        super();
    }

    public GioKey(@Nullable String attributeName, String id, GioKeyForType forType,
                  @Nullable String defaultValue, @Nullable GioDataType attributeType) {
        super();
        this.attributeName = attributeName;
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.forType = Objects.requireNonNull(forType, "forType cannot be null");
        this.defaultValue = defaultValue;
        this.attributeType = attributeType;
    }

    public GioKey(@Nullable Map<String, String> customAttributes, @Nullable String description,
                  @Nullable String attributeName, String id, GioKeyForType forType,
                  @Nullable String defaultValue, @Nullable GioDataType attributeType) {
        super(customAttributes, description);
        this.attributeName = attributeName;
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.forType = Objects.requireNonNull(forType, "forType cannot be null");
        this.defaultValue = defaultValue;
        this.attributeType = attributeType;
    }

    // Builder
    public static GioKeyBuilder builder() {
        return new GioKeyBuilder();
    }

    public static class GioKeyBuilder {
        private @Nullable Map<String, String> customAttributes;
        private @Nullable String description;
        private @Nullable String attributeName;
        private String id;
        private GioKeyForType forType;
        private @Nullable String defaultValue;
        private @Nullable GioDataType attributeType;

        public GioKeyBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioKeyBuilder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public GioKeyBuilder attributeName(@Nullable String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public GioKeyBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GioKeyBuilder forType(GioKeyForType forType) {
            this.forType = forType;
            return this;
        }

        public GioKeyBuilder defaultValue(@Nullable String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public GioKeyBuilder attributeType(@Nullable GioDataType attributeType) {
            this.attributeType = attributeType;
            return this;
        }

        public GioKey build() {
            return new GioKey(customAttributes, description, attributeName, id, forType, defaultValue, attributeType);
        }
    }

    // Getters and Setters
    public @Nullable String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(@Nullable String attributeName) {
        this.attributeName = attributeName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
    }

    public GioKeyForType getForType() {
        return forType;
    }

    public void setForType(GioKeyForType forType) {
        this.forType = Objects.requireNonNull(forType, "forType cannot be null");
    }

    public @Nullable String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(@Nullable String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public @Nullable GioDataType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(@Nullable GioDataType attributeType) {
        this.attributeType = attributeType;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioKey gioKey = (GioKey) o;
        return Objects.equals(attributeName, gioKey.attributeName) &&
               Objects.equals(id, gioKey.id) &&
               forType == gioKey.forType &&
               Objects.equals(defaultValue, gioKey.defaultValue) &&
               attributeType == gioKey.attributeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attributeName, id, forType, defaultValue, attributeType);
    }

    @Override
    public String toString() {
        return "GioKey{" +
               "attributeName='" + attributeName + '\'' +
               ", id='" + id + '\'' +
               ", forType=" + forType +
               ", defaultValue='" + defaultValue + '\'' +
               ", attributeType=" + attributeType +
               ", description='" + getDescription() + '\'' +
               ", customAttributes=" + getCustomAttributes() +
               '}';
    }

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
