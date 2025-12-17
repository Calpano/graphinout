package com.graphinout.base.cj.document.impl;

import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjDocumentMetaMutable;
import com.graphinout.base.cj.writer.ICjWriter;
import org.jspecify.annotations.Nullable;

public class CjDocumentMetaElement implements ICjDocumentMetaMutable {

    private String versionNumber;
    private String versionDate;
    private Boolean canonical;

    @Override
    public ICjDocumentMetaMutable canonical(Boolean canonical) {
        this.canonical = canonical;
        return this;
    }

    @Nullable
    @Override
    public Boolean canonical() {
        return canonical;
    }

    @Override
    public CjType cjType() {
        return CjType.ConnectedJson;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        // alphabetic order
        cjWriter.connectedJsonStart();
        cjWriter.maybe(canonical, cjWriter::connectedJson__canonical);
        cjWriter.maybe(versionDate, cjWriter::connectedJson__versionDate);
        cjWriter.maybe(versionNumber, cjWriter::connectedJson__versionNumber);
        cjWriter.connectedJsonEnd();
    }

    @Override
    public ICjDocumentMetaMutable versionDate(String versionDate) {
        this.versionDate = versionDate;
        return this;
    }

    @Nullable
    @Override
    public String versionDate() {
        return versionDate;
    }

    @Override
    public ICjDocumentMetaMutable versionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
        return this;
    }

    @Nullable
    @Override
    public String versionNumber() {
        return versionNumber;
    }

}
