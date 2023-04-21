package com.calpano.graphinout.base.gio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public abstract class GioElementWithDescription extends GioElement {

    /**
     * This ia an Element That can be empty or null.
     * <p>
     * The name of this element in graph is <b>desc</b>
     */
    protected @Nullable String description;

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

}
