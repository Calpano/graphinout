package com.graphinout.base.gio;

import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class GioElementWithDescription extends GioElement {

    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected @Nullable XmlFragmentString description;

    public GioElementWithDescription() {
        super();
    }

    public GioElementWithDescription(@Nullable XmlFragmentString description) {
        super();
        this.description = description;
    }

    public GioElementWithDescription(@Nullable Map<String, String> customAttributes, @Nullable XmlFragmentString description) {
        super(customAttributes);
        this.description = description;
    }

    public Optional<XmlFragmentString> description() {
        return Optional.ofNullable(description);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GioElementWithDescription that = (GioElementWithDescription) o;
        return Objects.equals(description, that.description);
    }


    public @Nullable XmlFragmentString getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description);
    }

    public void setDescription(@Nullable XmlFragmentString description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "GioElementWithDescription{" +
                "description='" + description + '\'' +
                ", customAttributes=" + getCustomAttributes() +
                '}';
    }

}
