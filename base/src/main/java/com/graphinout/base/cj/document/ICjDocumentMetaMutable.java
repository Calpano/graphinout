package com.graphinout.base.cj.document;

public interface ICjDocumentMetaMutable extends ICjDocumentMeta {

    ICjDocumentMetaMutable canonical(Boolean canonical);

    ICjDocumentMetaMutable versionNumber(String versionNumber);

    ICjDocumentMetaMutable versionDate(String versionDate);

}
