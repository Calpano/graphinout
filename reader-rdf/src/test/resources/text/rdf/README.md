# RDF Test Data

Sample files in various RDF serialization formats collected from public sources.

## Formats and Sources

### TTL (Turtle)
- **W3C Test Suite**: Basic syntax tests from W3C RDF 1.1 Turtle test suite
- **W3C Ontologies**: PROV, DCAT, SHACL, ORG vocabularies, RDF syntax namespace
- **DBpedia**: Berlin entity data
- **EasyRdf**: FOAF example
- **Tim Berners-Lee's FOAF card**: Personal profile document

### NT (N-Triples)
- **W3C Test Suite**: Literal, language tag, UTF-8, and syntax tests
- **DBpedia**: Berlin entity data
- **EasyRdf**: FOAF example

### NQ (N-Quads)
- **W3C Test Suite**: URI, blank node, literal, and language tag tests

### TRIG (TriG)
- **W3C Test Suite**: Named graph, collection, and syntax tests

### RDF/XML
- **W3C Test Suite**: Datatypes, XML lang, URL encoding tests
- **FOAF Ontology**: Complete FOAF vocabulary definition
- **DBpedia**: Berlin entity data
- **EasyRdf**: FOAF example

### JSON-LD
- **W3C Test Suite**: Basic JSON-LD to RDF conversion tests
- **Schema.org**: Complete vocabulary (large file)
- **DBpedia**: Berlin entity data
- **EasyRdf**: FOAF example

### TRIX (TriX - Triples in XML)
- **RDFLib Test Suite**: Example, aperture, Jena compatibility, and blank node tests
- **Ruby-RDF**: Examples with URIs, literals, and typed literals

### RJ (RDF JSON)
- **EasyRdf**: FOAF examples in both standard and triples format
- Note: RDF JSON (.rj) is deprecated in favor of JSON-LD

### TRDF (RDF Thrift) and RPB (RDF Protobuf)
**These are binary formats** that require Apache Jena tools to generate.

To create .trdf or .rpb files from existing RDF data, use Jena's `riot` command:

```bash
# Convert Turtle to RDF Thrift
riot --out=RDF_THRIFT input.ttl > output.trdf

# Convert Turtle to RDF Protobuf
riot --out=RDF_PROTOBUF input.ttl > output.rpb
```

## Sources

- **W3C RDF Test Suites**: https://w3c.github.io/rdf-tests/
- **DBpedia**: https://dbpedia.org/
- **Schema.org**: https://schema.org/
- **FOAF Ontology**: http://xmlns.com/foaf/spec/
- **W3C Ontologies**: https://www.w3.org/ns/
- **EasyRdf Test Fixtures**: https://github.com/easyrdf/easyrdf
- **RDFLib Test Suite**: https://github.com/RDFLib/rdflib
- **Ruby-RDF TriX**: https://github.com/ruby-rdf/rdf-trix
