package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * The root document of a Gio model graph stream.
 */
public class GioDocument extends GioElementWithDescription {

    public static class GioDocumentBuilder {

        private @Nullable Map<String, String> customAttributes;
        private @Nullable XmlFragmentString description;

        public GioDocument build() {
            return new GioDocument(customAttributes, description);
        }

        public GioDocumentBuilder customAttributes(@Nullable Map<String, String> customAttributes) {
            this.customAttributes = customAttributes;
            return this;
        }

        public GioDocumentBuilder description(@Nullable XmlFragmentString description) {
            this.description = description;
            return this;
        }

    }

    public GioDocument() {
        super();
    }

    public GioDocument(@Nullable XmlFragmentString description) {
        super(description);
    }

    public GioDocument(@Nullable Map<String, String> customAttributes, @Nullable XmlFragmentString description) {
        super(customAttributes, description);
    }

    public static GioDocumentBuilder builder() {
        return new GioDocumentBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return "GioDocument{" +
                "description='" + getDescription() + '\'' +
                ", customAttributes=" + getCustomAttributes() +
                '}';
    }

}
