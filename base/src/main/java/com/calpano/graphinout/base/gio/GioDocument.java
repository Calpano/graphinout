package com.calpano.graphinout.base.gio;

import java.util.LinkedHashMap;
import java.util.List;

import com.calpano.graphinout.base.XMLValue;
import com.calpano.graphinout.base.util.GIOUtil;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

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
