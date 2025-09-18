package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.element.ICjDocumentMeta;

public interface ICjDocumentMetaMutable extends ICjDocumentMeta {

    void versionNumber(String versionNumber);

    void versionDate(String versionDate);

}
