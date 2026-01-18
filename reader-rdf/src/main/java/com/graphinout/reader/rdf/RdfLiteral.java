package com.graphinout.reader.rdf;

import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.jspecify.annotations.Nullable;

class RdfLiteral {

    public enum Kind {
        Plain, DataTyped, LanguageTagged
    }

    final IJsonObject o;

    RdfLiteral(IJsonObject o) {this.o = o;}

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

    public @Nullable String value() {
        return o.getString("value");
    }

}
