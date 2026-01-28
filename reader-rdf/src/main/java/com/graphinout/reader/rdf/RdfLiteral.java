package com.graphinout.reader.rdf;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.jspecify.annotations.Nullable;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

record RdfLiteral(IJsonObjectMutable jsonObject) {

    public enum Kind {
        Plain, DataTyped, LanguageTagged
    }

    public static final String LANGUAGE = "language";
    public static final String VALUE = "value";
    public static final String DATATYPE = "datatype";

    public static RdfLiteral of(Literal literal) {
        IJsonObjectMutable litObj = IJsonFactory.INSTANCE.createObjectMutable();
        RdfLiteral rdfLiteral = new RdfLiteral(litObj);
        rdfLiteral.value(literal.getLexicalForm());
        ifPresentAccept(literal.getDatatypeURI(), rdfLiteral::datatype);
        ifPresentAccept(literal.getLanguage(), rdfLiteral::language);
        return rdfLiteral;
    }

    public @Nullable String datatype() {
        if (language() != null) return RDF.langString.getURI();
        return jsonObject.getString(DATATYPE);
    }

    public void datatype(@Nullable String datatype) {
        if (XSD.xstring.getURI().equals(datatype) || RDF.langString.getURI().equals(datatype)) return;
        jsonObject.setString(DATATYPE, datatype);
    }

    public boolean isDataTyped() {
        String dt = datatype();
        return dt != null && !dt.equals(XSD.xstring.getURI()) && !dt.equals(RDF.langString.getURI());
    }

    public boolean isLanguageTagged() {
        return language() != null;
    }

    public boolean isPlain() {
        return !isLanguageTagged() && !isDataTyped();
    }

    public Kind kind() {
        if (isLanguageTagged()) {
            return Kind.LanguageTagged;
        } else if (isDataTyped()) {
            return Kind.DataTyped;
        } else {
            return Kind.Plain;
        }
    }

    public @Nullable String language() {
        return jsonObject.getString(LANGUAGE);
    }

    public void language(@Nullable String language) {
        if (language == null) {
            jsonObject.removeProperty(LANGUAGE);
        } else if (!language.isBlank()) {
            jsonObject.setString(LANGUAGE, language);
        }
    }

    /**
     * @param model used as a factory
     * @return a JENA RDF {@link Literal} representing this RdfLiteral
     */
    public Literal toRdfLiteral(Model model) {
        return switch (kind()) {
            case Plain -> model.createLiteral(value());
            case LanguageTagged -> model.createLiteral(value(), language());
            case DataTyped -> model.createTypedLiteral(value(), datatype());
        };
    }

    public @Nullable String value() {
        return jsonObject.getString(VALUE);
    }

    public void value(@Nullable String value) {
        jsonObject.setString(VALUE, value);
    }


}
