package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.ICjDocumentMetaMutable;
import com.calpano.graphinout.base.cj.stream.ICjWriter;

import javax.annotation.Nullable;

public class CjDocumentMetaElement implements ICjDocumentMetaMutable {

    private String versionNumber;
    private String versionDate;
    private Boolean canonical;

    @Override
    public void canonical(Boolean canonical) {
        this.canonical = canonical;
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
