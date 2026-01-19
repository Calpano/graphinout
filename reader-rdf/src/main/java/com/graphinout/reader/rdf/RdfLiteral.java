package com.graphinout.reader.rdf;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.jspecify.annotations.Nullable;

record RdfLiteral(IJsonObject o) {

    public enum Kind {
        Plain, DataTyped, LanguageTagged
    }

    public @Nullable String datatype() {
        return o.getString("datatype");
    }

    public Kind kind() {
        if (language() != null) {
            return Kind.LanguageTagged;
        } else if (datatype() != null) {
            return Kind.DataTyped;
        } else {
            return Kind.Plain;
        }
    }

    public @Nullable String language() {
        return o.getString("language");
    }

    public Literal toRdfLiteral(Model model) {
        return switch (kind()) {
            case Plain -> model.createLiteral(value());
            case LanguageTagged -> model.createLiteral(value(), language());
            case DataTyped -> model.createTypedLiteral(value(), datatype());
        };
    }

    public @Nullable String value() {
        return o.getString("value");
    }

}
