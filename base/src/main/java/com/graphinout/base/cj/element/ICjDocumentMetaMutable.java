package com.graphinout.base.cj.element;

public interface ICjDocumentMetaMutable extends ICjDocumentMeta {

    void canonical(Boolean canonical);

    void versionNumber(String versionNumber);

    void versionDate(String versionDate);

}
