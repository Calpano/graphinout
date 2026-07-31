package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;

/**
 * Reads RDF Turtle.
 *
 * <p><strong>{@code .n3} is deliberately NOT claimed.</strong> It used to be, and was parsed as Turtle.
 * That works for the many {@code .n3} files which only use the Turtle subset, but it misrepresents the
 * format: Notation3 is a superset, and its own constructs — inverse predicates ({@code X is P of Y}),
 * formulae ({@code &#123;...&#125;}), the {@code @forAll}/{@code @forSome} quantifiers — are not Turtle. A
 * file using them failed with a raw parser error that read as though the data were malformed, when in fact
 * the data was valid N3 and we simply had no parser for it.
 *
 * <p>Parsing N3 properly is not an option we have: <strong>there is no Java N3 parser to depend on</strong>.
 * Jena 5.6 offers {@code Lang.N3} in name only — it is handled by the Turtle parser. Verified directly:
 * reading {@code w3c-timbl-card.n3} as {@code Lang.N3} and as {@code Lang.TURTLE} fails identically with
 * {@code [line: 235, col: 8] Unrecognized keyword: is}. That is why {@link RdfFormats.RdfSyntax} leaves N3
 * commented out with the note "as alias for Turtle". The surviving N3 implementations are JavaScript
 * (N3.js), EYE and a couple of Perl modules; and most of what N3 adds over Turtle is a rule language that
 * does not map onto a graph model anyway.
 *
 * <p>So this reader claims Turtle and only Turtle. Declaring a format we cannot parse is worse than
 * declaring none: it turns "graphinout has no N3 support" into what looks like "your file is broken".
 *
 * <p>A {@code .n3} file that actually holds Turtle should simply be named {@code .ttl}, which is what the
 * corpus now does — the former {@code text/notation3/dbpedia-Karlsruhe.n3} proved byte-identical to
 * {@code text/turtle/dbpedia-Karlsruhe.ttl}: a mislabelled copy, not an N3 sample.
 */
public class RdfTurtleReader extends RdfReader implements GioReader {

    public static final String FORMAT_ID = "turtle";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "RDF Turtle", ".ttl");

    public RdfTurtleReader() {super(FORMAT, RdfFormats.RdfSyntax.TURTLE);}

}
