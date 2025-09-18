package com.calpano.graphinout.base.cj.element.impl;

import javax.annotation.Nullable;

public class CjDocumentMetaElement implements ICjDocumentMetaMutable {


    private String versionNumber;
    private String versionDate;

    @Override
    public void versionDate(String versionDate) {
        this.versionDate = versionDate;
    }

    @Nullable
    @Override
    public String versionDate() {
        return versionDate;
    }

    @Override
    public void versionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Nullable
    @Override
    public String versionNumber() {
        return versionNumber;
    }

}
