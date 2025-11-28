package com.graphinout.base.cj.document.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjLabelEntryMutable;
import com.graphinout.base.cj.writer.ICjWriter;

import javax.annotation.Nullable;

public class CjLabelEntryElement extends CjHasDataElement implements ICjLabelEntryMutable {


    private @Nullable String language;
    private String value;
    private @Nullable JsonNode data;

    @Override
    public CjType cjType() {
        return CjType.LabelEntry;
    }

    @Override
    public void fire(ICjWriter cjWriter) {
        cjWriter.labelEntryStart();
        // alphabetic order
        cjWriter.maybe(language, cjWriter::language);
        cjWriter.value(value);
        cjWriter.labelEntryEnd();
    }

    @Nullable
    public JsonNode getData() {
        return data;
    }

    public void language(@Nullable String language) {
        this.language = language;
    }

    @Nullable
    @Override
    public String language() {
        return language;
    }

    public void setData(@Nullable JsonNode data) {
        this.data = data;
    }

    public void setLanguage(@Nullable String language) {
        this.language = language;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void value(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

}
