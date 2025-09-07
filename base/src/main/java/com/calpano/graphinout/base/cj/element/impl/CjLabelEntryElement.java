package com.calpano.graphinout.base.cj.element.impl;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.cj.element.ICjLabelProperties;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

public class CjLabelEntryElement implements ICjLabelProperties, ICjElement {

    private @Nullable String language;
    private String value;
    private @Nullable JsonNode data;

    @Override
    public CjType cjType() {
        return CjType.LabelEntry;
    }

    @Override
    public void fire(CjWriter cjWriter) {
        cjWriter.labelStart();
        cjWriter.value(value);
        ofNullable(language).ifPresent(cjWriter::language);
        cjWriter.labelEnd();
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
