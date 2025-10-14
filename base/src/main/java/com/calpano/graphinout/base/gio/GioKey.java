package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rbaba
 * @implNote A GraphML-Attribute is defined by a key element which specifies the identifier, name, type and domain of
 * the attribute.
 * <p>
 * The name of the GraphML-Attribute is defined by the XML-Attribute attr.name and must be unique among all
 * GraphML-Attributes declared in the document. The purpose of the name is that applications can identify the meaning of
 * the attribute. Note that the name of the GraphML-Attribute is not used inside the document, the identifier is used
 * for this purpose.
 * <p>
 * The type of the GraphML-Attribute can be either boolean, int, long, float, double, or string.
 * <p>
 * In GraphML there may be data-functions attached to graphs, nodes, ports, edges, hyperedges and endpoint and to the
 * whole collection of graphs described by the content of &lt;graphml&gt;. These functions are declared by &lt;key&gt;
 * elements (children of &lt;graphml&gt;) and defined by &lt;data&gt; elements. Occurence: &lt;graphml&gt;.
 * @see GioData {@link GioData}
 */
public class GioKey extends GioElementWithDescription {

    public static class GioKeyBuilder {

        private @Nullable Map<String, String> customAttributes;
        private @Nullable XmlFragmentString description;
        private @Nullable String attributeName;
        private String id;
        private GioKeyForType forType;
        private @Nullable XmlFragmentString defaultValue;
        private @Nullable GioDataType attributeType;

        public GioKeyBuilder attributeName(@Nullable String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public GioKeyBuilder attributeType(@Nullable GioDataType attributeType) {
            this.attributeType = attributeType;
            return this;
        }

        public GioKey build() {
            return new GioKey(customAttributes, description, attributeName, id, forType, defaultValue, attributeType);
        }

        public GioKeyBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioKeyBuilder defaultValue(@Nullable XmlFragmentString defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public GioKeyBuilder description(@Nullable XmlFragmentString description) {
            this.description = description;
            return this;
        }

        public GioKeyBuilder forType(GioKeyForType forType) {
            this.forType = forType;
            return this;
        }

        public GioKeyBuilder id(String id) {
            this.id = id;
            return this;
        }

    }

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
     * In XML, this is #PCDATA, so it may contain any mix of text and tags. Theoretically, this data could also be
     * large. But in practice, this is at most used to store icons, maybe up to a few megabytes.
     */
    private @Nullable XmlFragmentString defaultValue;
    /**
     * GraphML Type data / attribute extension
     */
    private @Nullable GioDataType attributeType;


    public GioKey() {
        super();
    }

    public GioKey(@Nullable String attributeName, String id, GioKeyForType forType,
                  @Nullable XmlFragmentString defaultValue, @Nullable GioDataType attributeType) {
        super();
        this.attributeName = attributeName;
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.forType = Objects.requireNonNull(forType, "forType cannot be null");
        this.defaultValue = defaultValue;
        this.attributeType = attributeType;
    }

    public GioKey(@Nullable Map<String, String> customAttributes, @Nullable XmlFragmentString description,
                  @Nullable String attributeName, String id, GioKeyForType forType,
                  @Nullable XmlFragmentString defaultValue, @Nullable GioDataType attributeType) {
        super(customAttributes, description);
        this.attributeName = attributeName;
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.forType = Objects.requireNonNull(forType, "forType cannot be null");
        this.defaultValue = defaultValue;
        this.attributeType = attributeType;
    }


    public static GioKeyBuilder builder() {
        return new GioKeyBuilder();
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

    public Optional<XmlFragmentString> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }


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


    public @Nullable String getAttributeName() {
        return attributeName;
    }

    public @Nullable GioDataType getAttributeType() {
        return attributeType;
    }

    public @Nullable XmlFragmentString getDefaultValue() {
        return defaultValue;
    }

    public GioKeyForType getForType() {
        return forType;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attributeName, id, forType, defaultValue, attributeType);
    }

    public void setAttributeName(@Nullable String attributeName) {
        this.attributeName = attributeName;
    }

    public void setAttributeType(@Nullable GioDataType attributeType) {
        this.attributeType = attributeType;
    }

    public void setDefaultValue(@Nullable XmlFragmentString defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setForType(GioKeyForType forType) {
        this.forType = Objects.requireNonNull(forType, "forType cannot be null");
    }

    public void setForType(String forType) throws IllegalArgumentException {
        this.forType = GioKeyForType.keyForType(forType);
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
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

}
