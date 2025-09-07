package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;

public interface ICjElement {

    CjType cjType();

    void fire(CjWriter cjWriter);

}
