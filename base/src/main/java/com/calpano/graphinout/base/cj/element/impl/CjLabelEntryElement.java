package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjLabelEntryMutable;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;

public class CjLabelEntryElement extends CjHasDataElement implements ICjLabelEntryMutable {

    private @Nullable String language;
    private String value;
    private @Nullable JsonNode data;

    CjLabelEntryElement(@Nullable CjElement parent) {
        super(parent);
    }

    @Override
    public CjType cjType() {
        return CjType.LabelEntry;
    }

    @Override
    public void fire(CjWriter cjWriter) {
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
