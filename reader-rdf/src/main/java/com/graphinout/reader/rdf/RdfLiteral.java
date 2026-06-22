package com.graphinout.reader.rdf;

import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

record RdfLiteral(IJsonObjectMutable jsonObject) {

    public enum Kind {
        Plain, DataTyped, LanguageTagged
    }

    public static final String LANGUAGE = "language";
    public static final String VALUE = "value";
    public static final String DATATYPE = "datatype";

    public static RdfLiteral of(Literal literal) {
        return of(literal, UnaryOperator.identity());
    }

    /**
     * Build an {@link RdfLiteral} from a Jena {@link Literal}, abbreviating the datatype IRI to a CURIE
     * via {@code datatypeAbbreviator} (typically {@code cjDoc::asId_}) so the datatype stored in CJ is
     * symmetric with node ids and predicates. The {@code xsd:string}/{@code rdf:langString} elision (see
     * {@link #datatype(String)}) is preserved: those datatypes never produce a {@code datatype} key, so the
     * abbreviator is not applied to them.
     */
    public static RdfLiteral of(Literal literal, UnaryOperator<String> datatypeAbbreviator) {
        IJsonObjectMutable litObj = IJsonFactory.INSTANCE.createObjectMutable();
        RdfLiteral rdfLiteral = new RdfLiteral(litObj);
        rdfLiteral.value(literal.getLexicalForm());
        ifPresentAccept(literal.getDatatypeURI(), dt -> rdfLiteral.datatype(dt, datatypeAbbreviator));
        ifPresentAccept(literal.getLanguage(), rdfLiteral::language);
        return rdfLiteral;
    }

    public @Nullable String datatype() {
        if (language() != null) return RDF.langString.getURI();
        return jsonObject.getString(DATATYPE);
    }

    public void datatype(@Nullable String datatype) {
        datatype(datatype, UnaryOperator.identity());
    }

    /**
     * Store the datatype, eliding {@code xsd:string}/{@code rdf:langString} (which never get a datatype
     * key) and otherwise abbreviating the (full IRI) {@code datatype} to a CURIE via {@code abbreviator}.
     * The elision is checked against the full IRI <em>before</em> abbreviation.
     */
    public void datatype(@Nullable String datatype, UnaryOperator<String> abbreviator) {
        if (XSD.xstring.getURI().equals(datatype) || RDF.langString.getURI().equals(datatype)) return;
        jsonObject.setString(DATATYPE, datatype == null ? null : abbreviator.apply(datatype));
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
        return toRdfLiteral(model, UnaryOperator.identity());
    }

    /**
     * Like {@link #toRdfLiteral(Model)}, but expands the (possibly CURIE) datatype back to a full IRI via
     * {@code datatypeExpander} (typically {@code curie -> CjUris.expandId(context, curie)}) before building
     * the typed literal — the inverse of the abbreviation done in {@link #of(Literal, UnaryOperator)}. A
     * datatype with no matching prefix is returned unchanged by {@code expandId} and so round-trips.
     */
    public Literal toRdfLiteral(Model model, UnaryOperator<String> datatypeExpander) {
        return switch (kind()) {
            case Plain -> model.createLiteral(value());
            case LanguageTagged -> model.createLiteral(value(), language());
            case DataTyped -> model.createTypedLiteral(value(), datatypeExpander.apply(datatype()));
        };
    }

    public @Nullable String value() {
        return jsonObject.getString(VALUE);
    }

    public void value(@Nullable String value) {
        jsonObject.setString(VALUE, value);
    }


}
