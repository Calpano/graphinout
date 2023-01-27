package com.calpano.graphinout.base.gio;

import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * The root document of a Gio model graph stream
 */
@Data
@SuperBuilder
public class GioDocument extends GioGraphCommonElement  {

    /**
     * This is an Element that can be empty or null.
     * </p>
     * The name of this Element in graphML is <b>key</b>
     */
    @Singular(ignoreNullCollections = true)
    private List<GioKey> keys;

}
