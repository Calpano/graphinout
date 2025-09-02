package com.calpano.graphinout.base.gio;

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
    protected @Nullable String description;


    public GioElementWithDescription() {
        super();
    }

    public GioElementWithDescription(@Nullable String description) {
        super();
        this.description = description;
    }

    public GioElementWithDescription(@Nullable Map<String, String> customAttributes, @Nullable String description) {
        super(customAttributes);
        this.description = description;
    }

    public Optional<String> description() {
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


    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description);
    }

    public void setDescription(@Nullable String description) {
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
