package com.calpano.graphinout.base.gio;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Optional;

/**
 * The root document of a Gio model graph stream.
 */
@Data
@SuperBuilder
public class GioDocument extends GioElementWithData {

    /**
     * Define default type and default values.
     * May be empty
     */
    List<GioKey> keys;


}
