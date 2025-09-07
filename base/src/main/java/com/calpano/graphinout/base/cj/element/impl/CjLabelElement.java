package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;

/**
 * A CJ document
 */
public class CjLabelElement extends CjArrayElement implements ICjElement {


    public CjLabelElement(CjElement parent) {
        super(parent, CjType.ArrayOfLabelEntries);
    }

}
