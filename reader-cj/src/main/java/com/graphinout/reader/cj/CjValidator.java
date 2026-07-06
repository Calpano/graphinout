package com.graphinout.reader.cj;


import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.input.ContentErrorList;
import com.graphinout.base.json.JsonSchemaValidator;
import com.graphinout.foundation.pure.collections.jajson.JsonParser;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.ValidatingJsonWriter;

import java.io.IOException;
import java.util.Map;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * Reads a text file and validates it.
 */
public class CjValidator {

    /**
     * Reject clearly non-URIs but let some illegal URIs pass to make validation cheaper
     *
     * @param uri
     * @return
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidUri(String uri) {
        // This is a very basic check. A full URI validation is complex and might require a dedicated library.
        // For now, we check for some common invalid characters and basic structure.
        if (uri == null || uri.isBlank()) {
            return false;
        }

        // Check for characters not allowed in URIs (excluding those that might be percent-encoded)
        // This regex is a simplified check and not fully RFC compliant.
        // It disallows spaces and some other characters that should typically be encoded.
        if (uri.matches(".*[\\s<>{}|\\[\\]`^\"\\\\].*")) {
            return false;
        }

        // Basic scheme check (e.g., http://, https://, urn:, mailto:)
        // This is a very lenient check, just ensuring it starts with something that looks like a scheme.
        if (!uri.contains(":") || uri.startsWith(":") || uri.endsWith(":")) {
            return false;
        }

        try {
            new java.net.URI(uri);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static void validateCj(String cjJson, ContentErrorList errors) {
        validateJson(cjJson, errors);
        if (!errors.isEmpty()) return;

        // first, validate via JSON schema
        JsonSchemaValidator.isValidCj(cjJson, errors::add);

        if (!errors.isEmpty()) return;

        validateCjReferences(cjJson, errors);
    }

    /**
     * Is it valid CJ: All endpoint.node ids resolve to existing nodes in same document? All namespace URIs in
     * {@code @context} are valid URIs? Also the expanded URIs (context + edge.type, context +
     * node.types) ? Edge with 0 or 1 endpoint: warn Empty graph: warn
     *
     * @param cjJson
     * @param errors
     */
    public static void validateCjReferences(String cjJson, ContentErrorList errors) {
        try {
            ICjDocument cjDoc = CjDocuments.parseCjJsonString("validation", cjJson);

            // All endpoint.node ids resolve to existing nodes in same document?
            cjDoc.edgesAll().flatMap(ICjEdge::endpoints).forEach(endpoint -> {
                if (endpoint.node() != null) {
                    if (cjDoc.findNodeById(endpoint.node()) == null) {
                        errors.add(ContentError.info("Reference: Endpoint references non-defined node with ID '" + endpoint.node() + "'"));
                    }
                }
            });

            // All namespace URIs in @context are valid?
            ifPresentAccept(cjDoc.context(), (Map<String, String> context) -> {
                for (Map.Entry<String, String> entry : context.entrySet()) {
                    if (!isValidUri(entry.getValue())) {
                        errors.add(ContentError.error("URI Error: Namespace URI for prefix '" + entry.getKey() + "' is not a valid URI: '" + entry.getValue() + "'"));
                    }
                }
            });
            // Expanded URIs: context + edge.type,
            cjDoc.edgesAll().forEach(edge -> {
                ifPresentAccept(edge.edgeType(), edgeType -> {
                    //  expand edge type ID via @context and validate resulting URI
                    String uri = cjDoc.uri(edgeType.type());
                    if (!isValidUri(uri)) {
                        errors.add(ContentError.error("URI Error: Edge type URI from String '" + uri + "' is not a valid URI"));
                    }
                });
            });

            // Expanded URIs: context + node.types
            cjDoc.nodesAll().forEach(node -> {
                node.types().forEach(nodeType -> {
                    // expand node type ID via @context and validate resulting URI
                    String uri = cjDoc.uri(nodeType.type());
                    if (!isValidUri(uri)) {
                        errors.add(ContentError.error("URI Error: Node type URI from String '" + uri + "' is not a valid URI"));
                    }
                });
            });

            // check for empty graphs or edges
            cjDoc.graphs().forEach(graph -> {
                if (graph.nodes().findAny().isEmpty() && graph.edges().findAny().isEmpty()
                        && graph.graphs().findAny().isEmpty()) {
                    errors.add(ContentError.warn("Graph '" + graph.id() + "' has no nodes or edges -- legal, but maybe an error?"));
                }
            });
            cjDoc.edgesAll().forEach(edge -> {
                if (edge.endpoints().findAny().isEmpty()) {
                    errors.add(ContentError.warn("Edge '" + edge.id() + "' has no endpoints -- legal, but maybe an error?"));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void validateJson(String json, ContentErrorList errors) {
        if (json == null || json.isEmpty()) {
            return;
        }

        validateUnicode(json, errors);
        if (!errors.isEmpty()) return;

        // Check for control characters (excluding common whitespace like tab, newline, carriage return)
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            // JSON allows specific control characters like \t, \n, \r, \f, \b
            // However, general control characters (0x00-0x1F) are not allowed unless escaped.
            // Here we are checking for unescaped control characters that are not common whitespace.
            if (c < 0x20 && c != 0x09 && c != 0x0A && c != 0x0D) {
                errors.add(ContentError.error("JSON Error: Unescaped control character at index " + i + " (char code: " + (int) c + ")"));
            }
        }

        JsonWriter jw = new ValidatingJsonWriter();
        jw.setContentErrorHandler(errors::add);
        try {
            JsonParser.parse(json, jw);
        } catch (ContentErrorException e) {
            errors.add(ContentError.of(e.errorLevel(), e.getMessage(), e.location()));
        }
    }

    /**
     * Validate that each codepoint is either not a surrogate pair OR or correctly paired.
     *
     * @param s
     * @return a list of errors, or an empty list.
     */
    public static ContentErrorList validateUnicode(String s) {
        ContentErrorList errors = ContentErrorList.create();
        validateUnicode(s, errors);
        return errors;
    }

    /**
     * Is it legal Unicode? No unpaired surrogate pairs. Is it legal JSON? No control characters, valid structure.
     *
     * @param s
     * @param consumer
     */
    public static void validateUnicode(String s, ContentErrorList consumer) {
        if (s == null || s.isEmpty()) {
            return;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 < s.length()) {
                    char nextC = s.charAt(i + 1);
                    if (!Character.isLowSurrogate(nextC)) {
                        consumer.add(ContentError.error("Unicode Error: Unpaired high surrogate at index " + i));
                    }
                    i++; // Skip the low surrogate as it's part of the pair
                } else {
                    consumer.add(ContentError.error("Unicode Error: Unpaired high surrogate at end of string at index " + i));
                }
            } else if (Character.isLowSurrogate(c)) {
                consumer.add(ContentError.error("Unicode Error: Unpaired low surrogate at index " + i));
            }
        }
    }

}
