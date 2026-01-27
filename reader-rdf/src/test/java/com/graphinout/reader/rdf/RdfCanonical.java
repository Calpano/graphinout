package com.graphinout.reader.rdf;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RDF Dataset Canonicalization Algorithm (RDFC-1.0) <a href="https://www.w3.org/TR/rdf-canon">SPEC</a>
 * <p>
 * This implementation canonicalizes blank node identifiers in RDF graphs to produce a deterministic, normalized
 * representation.
 */
public class RdfCanonical {

    /**
     * Issues canonical identifiers for blank nodes.
     */
    private static class IdentifierIssuer {

        private final String prefix;
        private final Map<Resource, String> issuedIdentifiers = new HashMap<>();
        private int counter = 0;

        public IdentifierIssuer(String prefix) {
            this.prefix = prefix;
        }

        public IdentifierIssuer copy() {
            IdentifierIssuer copy = new IdentifierIssuer(this.prefix);
            copy.counter = this.counter;
            copy.issuedIdentifiers.putAll(this.issuedIdentifiers);
            return copy;
        }

        public String getId(Resource blankNode) {
            if (issuedIdentifiers.containsKey(blankNode)) {
                return issuedIdentifiers.get(blankNode);
            }

            String newId = prefix + counter;
            counter++;
            issuedIdentifiers.put(blankNode, newId);
            return newId;
        }

        public boolean hasId(Resource blankNode) {
            return issuedIdentifiers.containsKey(blankNode);
        }

    }

    /**
     * Result from Hash N-Degree Quads containing hash and issuer state.
     */
    private static class HashNDegreeResult {

        final String hash;
        final IdentifierIssuer issuer;

        HashNDegreeResult(String hash, IdentifierIssuer issuer) {
            this.hash = hash;
            this.issuer = issuer;
        }

    }

    /**
     * Canonicalizes blank nodes in an RDF model according to RDFC-1.0. Note: This is a simplified implementation that
     * handles basic cases. A full RDFC-1.0 implementation requires handling all permutations as specified in the W3C
     * spec.
     *
     * @param rdfModel the RDF model to canonicalize
     * @return a new model with canonicalized blank node identifiers
     */
    public static Model canonicalBlankNodes(Model rdfModel) {
        // Step 1: Create blank node to quads mapping
        Map<Resource, List<Statement>> blankNodeToQuads = new HashMap<>();
        StmtIterator stmts = rdfModel.listStatements();

        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            Resource subject = stmt.getSubject();
            RDFNode object = stmt.getObject();

            // Map blank nodes to their statements
            if (subject.isAnon()) {
                blankNodeToQuads.computeIfAbsent(subject, k -> new ArrayList<>()).add(stmt);
            }
            if (object.isAnon()) {
                blankNodeToQuads.computeIfAbsent(object.asResource(), k -> new ArrayList<>()).add(stmt);
            }
        }

        // Step 2: Compute first-degree hashes for all blank nodes
        Map<String, List<Resource>> hashGroups = new HashMap<>();

        for (Resource blankNode : blankNodeToQuads.keySet()) {
            String hash = hashFirstDegreeQuads(blankNode, blankNodeToQuads);
            hashGroups.computeIfAbsent(hash, k -> new ArrayList<>()).add(blankNode);
        }

        // Step 3: Canonical identifier issuer
        IdentifierIssuer canonicalIssuer = new IdentifierIssuer("c14n");

        // Step 4: Issue canonical identifiers for unique hashes
        List<String> sortedHashes = new ArrayList<>(hashGroups.keySet());
        Collections.sort(sortedHashes);

        for (String hash : sortedHashes) {
            List<Resource> blankNodes = hashGroups.get(hash);
            if (blankNodes.size() == 1) {
                canonicalIssuer.getId(blankNodes.getFirst());
            }
        }

        // Step 5: Process non-unique hashes with n-degree algorithm
        for (String hash : sortedHashes) {
            List<Resource> blankNodes = hashGroups.get(hash);
            if (blankNodes.size() > 1) {
                // Compute n-degree hashes
                Map<String, Resource> nDegreeHashes = new HashMap<>();

                for (Resource blankNode : blankNodes) {
                    if (!canonicalIssuer.hasId(blankNode)) {
                        IdentifierIssuer tempIssuer = new IdentifierIssuer("b");
                        tempIssuer.getId(blankNode);
                        HashNDegreeResult result = hashNDegreeQuads(blankNode, blankNodeToQuads, canonicalIssuer, tempIssuer);
                        nDegreeHashes.put(result.hash, blankNode);
                    }
                }

                // Issue canonical identifiers in sorted n-degree hash order
                List<String> sortedNDegreeHashes = new ArrayList<>(nDegreeHashes.keySet());
                Collections.sort(sortedNDegreeHashes);

                for (String nDegreeHash : sortedNDegreeHashes) {
                    canonicalIssuer.getId(nDegreeHashes.get(nDegreeHash));
                }
            }
        }

        // Step 6: Serialize to canonical N-Quads and re-parse to create model with canonical blank node identifiers
        StringBuilder canonicalNQuads = new StringBuilder();
        List<String> sortedStatements = new ArrayList<>();

        StmtIterator allStmts = rdfModel.listStatements();
        while (allStmts.hasNext()) {
            Statement stmt = allStmts.nextStatement();
            StringBuilder nquad = new StringBuilder();

            // Subject
            Resource subject = stmt.getSubject();
            if (subject.isAnon()) {
                nquad.append("_:").append(canonicalIssuer.getId(subject));
            } else {
                nquad.append("<").append(subject.getURI()).append(">");
            }
            nquad.append(" ");

            // Predicate
            nquad.append("<").append(stmt.getPredicate().getURI()).append(">");
            nquad.append(" ");

            // Object
            RDFNode object = stmt.getObject();
            if (object.isAnon()) {
                nquad.append("_:").append(canonicalIssuer.getId(object.asResource()));
            } else if (object.isResource()) {
                nquad.append("<").append(object.asResource().getURI()).append(">");
            } else if (object.isLiteral()) {
                Literal lit = object.asLiteral();
                nquad.append("\"").append(escapeString(lit.getLexicalForm())).append("\"");
                if (lit.getLanguage() != null && !lit.getLanguage().isEmpty()) {
                    nquad.append("@").append(lit.getLanguage());
                } else if (lit.getDatatypeURI() != null) {
                    nquad.append("^^<").append(lit.getDatatypeURI()).append(">");
                }
            }

            nquad.append(" .");
            sortedStatements.add(nquad.toString());
        }

        // Sort statements for canonical order
        Collections.sort(sortedStatements);
        for (String stmt : sortedStatements) {
            canonicalNQuads.append(stmt).append("\n");
        }

        // Parse back to create model - use RDFDataMgr to preserve blank node label order
        Model canonicalModel = ModelFactory.createDefaultModel();
        canonicalModel.setNsPrefixes(rdfModel.getNsPrefixMap());
        java.io.StringReader reader = new java.io.StringReader(canonicalNQuads.toString());
        org.apache.jena.riot.RDFDataMgr.read(canonicalModel, reader, null, org.apache.jena.riot.Lang.NTRIPLES);

        return canonicalModel;
    }

    /**
     * Escape special characters in string literals for N-Triples format.
     */
    private static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Generate all permutations of a list.
     */
    private static <T> List<List<T>> generatePermutations(List<T> list) {
        List<List<T>> result = new ArrayList<>();
        if (list.isEmpty()) {
            result.add(new ArrayList<>());
            return result;
        }

        permuteHelper(new ArrayList<>(list), 0, result);
        return result;
    }

    /**
     * Computes the first-degree hash for a blank node. This hash is based on the immediate quad relationships of the
     * node.
     */
    private static String hashFirstDegreeQuads(Resource blankNode, Map<Resource, List<Statement>> blankNodeToQuads) {
        List<String> nquads = new ArrayList<>();
        List<Statement> statements = blankNodeToQuads.get(blankNode);

        for (Statement stmt : statements) {
            String nquad = serializeStatementForHashing(stmt, blankNode, null);
            nquads.add(nquad);
        }

        // Sort in Unicode code point order
        Collections.sort(nquads);

        // Concatenate and hash
        String concatenated = String.join("", nquads);
        return sha256Hash(concatenated);
    }

    /**
     * Computes the n-degree hash for a blank node. This recursively explores relationships between blank nodes and
     * evaluates all possible permutations to find the lexicographically lowest hash.
     */
    private static HashNDegreeResult hashNDegreeQuads(Resource blankNode, Map<Resource, List<Statement>> blankNodeToQuads, IdentifierIssuer canonicalIssuer, IdentifierIssuer issuer) {
        // Collect related blank nodes grouped by their hash (Hash Related Blank Node)
        Map<String, List<Resource>> hashToRelated = new HashMap<>();
        List<Statement> statements = blankNodeToQuads.getOrDefault(blankNode, new ArrayList<>());

        for (Statement stmt : statements) {
            Resource subject = stmt.getSubject();
            RDFNode object = stmt.getObject();

            // Find related blank nodes
            Resource related = null;
            String position = null;
            if (subject.isAnon() && !subject.equals(blankNode)) {
                related = subject;
                position = "s";
            } else if (object.isAnon() && !object.equals(blankNode)) {
                related = object.asResource();
                position = "o";
            }

            if (related != null && !canonicalIssuer.hasId(related)) {
                // Hash Related Blank Node algorithm
                String relatedHash = hashRelatedBlankNode(related, blankNode, stmt.getPredicate(), position, canonicalIssuer, issuer, blankNodeToQuads);
                hashToRelated.computeIfAbsent(relatedHash, k -> new ArrayList<>()).add(related);
            }
        }

        // Build data to hash by processing each hash group
        StringBuilder dataToHash = new StringBuilder();
        List<String> sortedHashes = new ArrayList<>(hashToRelated.keySet());
        Collections.sort(sortedHashes);

        for (String hash : sortedHashes) {
            dataToHash.append(hash);

            List<Resource> relatedNodes = hashToRelated.get(hash);

            String chosenPath = null;
            IdentifierIssuer chosenIssuer = null;

            // Try all permutations to find lexicographically-first path
            List<List<Resource>> permutations = generatePermutations(relatedNodes);

            for (List<Resource> permutation : permutations) {
                IdentifierIssuer issuerCopy = issuer.copy();
                StringBuilder path = new StringBuilder();
                List<Resource> recursionList = new ArrayList<>();

                // Process each node in permutation
                for (Resource related : permutation) {
                    if (canonicalIssuer.hasId(related)) {
                        path.append(canonicalIssuer.getId(related));
                    } else {
                        if (!issuerCopy.hasId(related)) {
                            recursionList.add(related);
                        }
                        path.append(issuerCopy.getId(related));
                    }

                    // Skip if path is already worse than chosen
                    if (chosenPath != null && path.length() >= chosenPath.length() && path.toString().compareTo(chosenPath) > 0) {
                        break;
                    }
                }

                // If we didn't skip, process recursion list
                if (chosenPath == null || path.toString().compareTo(chosenPath) <= 0) {
                    for (Resource related : recursionList) {
                        HashNDegreeResult result = hashNDegreeQuads(related, blankNodeToQuads, canonicalIssuer, issuerCopy);
                        path.append(issuerCopy.getId(related));
                        path.append("<").append(result.hash).append(">");
                        issuerCopy = result.issuer;

                        // Skip if path is now worse
                        if (chosenPath != null && path.toString().compareTo(chosenPath) > 0) {
                            break;
                        }
                    }
                }

                // Update chosen path if this is better
                if (chosenPath == null || path.toString().compareTo(chosenPath) < 0) {
                    chosenPath = path.toString();
                    chosenIssuer = issuerCopy;
                }
            }

            dataToHash.append(chosenPath);
            if (chosenIssuer != null) {
                issuer = chosenIssuer;
            }
        }

        // Serialize quads for the reference node with issuer
        List<String> nquads = new ArrayList<>();
        for (Statement stmt : statements) {
            String nquad = serializeStatementForHashing(stmt, blankNode, issuer);
            nquads.add(nquad);
        }
        Collections.sort(nquads);
        dataToHash.append(String.join("", nquads));

        return new HashNDegreeResult(sha256Hash(dataToHash.toString()), issuer);
    }

    /**
     * Hash Related Blank Node algorithm - generates a hash for a related blank node including its position and
     * predicate.
     */
    private static String hashRelatedBlankNode(Resource related, Resource reference, Property predicate, String position, IdentifierIssuer canonicalIssuer, IdentifierIssuer issuer, Map<Resource, List<Statement>> blankNodeToQuads) {
        StringBuilder input = new StringBuilder();

        // Add position
        input.append(position);

        // Add predicate (always, not just for non-graph position)
        input.append("<").append(predicate.getURI()).append(">");

        // Add identifier
        if (canonicalIssuer.hasId(related)) {
            input.append(canonicalIssuer.getId(related));
        } else if (issuer.hasId(related)) {
            input.append(issuer.getId(related));
        } else {
            input.append(hashFirstDegreeQuads(related, blankNodeToQuads));
        }

        return sha256Hash(input.toString());
    }

    private static <T> void permuteHelper(List<T> list, int start, List<List<T>> result) {
        if (start >= list.size()) {
            result.add(new ArrayList<>(list));
            return;
        }

        for (int i = start; i < list.size(); i++) {
            Collections.swap(list, start, i);
            permuteHelper(list, start + 1, result);
            Collections.swap(list, start, i);
        }
    }

    /**
     * Serializes a statement for hashing purposes. Replaces the reference blank node with "_:a" and other blank nodes
     * with "_:z".
     */
    private static String serializeStatementForHashing(Statement stmt, Resource referenceNode, IdentifierIssuer issuer) {
        StringBuilder sb = new StringBuilder();

        // Subject
        Resource subject = stmt.getSubject();
        if (subject.equals(referenceNode)) {
            sb.append("_:a");
        } else if (subject.isAnon()) {
            if (issuer != null && issuer.hasId(subject)) {
                sb.append("_:").append(issuer.getId(subject));
            } else {
                sb.append("_:z");
            }
        } else {
            sb.append("<").append(subject.getURI()).append(">");
        }

        sb.append(" ");

        // Predicate
        sb.append("<").append(stmt.getPredicate().getURI()).append(">");
        sb.append(" ");

        // Object
        RDFNode object = stmt.getObject();
        if (object.isResource() && object.asResource().equals(referenceNode)) {
            sb.append("_:a");
        } else if (object.isAnon()) {
            if (issuer != null && issuer.hasId(object.asResource())) {
                sb.append("_:").append(issuer.getId(object.asResource()));
            } else {
                sb.append("_:z");
            }
        } else if (object.isResource()) {
            sb.append("<").append(object.asResource().getURI()).append(">");
        } else if (object.isLiteral()) {
            Literal lit = object.asLiteral();
            sb.append("\"").append(lit.getLexicalForm()).append("\"");
            if (lit.getLanguage() != null && !lit.getLanguage().isEmpty()) {
                sb.append("@").append(lit.getLanguage());
            } else if (lit.getDatatypeURI() != null) {
                sb.append("^^<").append(lit.getDatatypeURI()).append(">");
            }
        }

        sb.append(" .\n");

        return sb.toString();
    }

    /**
     * Computes SHA-256 hash of input string.
     */
    private static String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

}
