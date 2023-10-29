package com.calpano.graphinout.base.gio;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Optional;

/**
 * The root document of a Gio model graph stream.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class GioDocument extends GioElementWithDescription {

}
