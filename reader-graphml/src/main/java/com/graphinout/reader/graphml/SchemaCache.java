package com.graphinout.reader.graphml;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import org.slf4j.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Per-JVM cache for the expensive, input-independent GraphML schema resources.
 * <p>
 * Two things were previously recomputed on every single read/validation:
 * <ol>
 *   <li>a full ClassGraph classpath scan for {@code *.xsd.xml} schema files, and</li>
 *   <li>compilation of the GraphML 1.1 XSD into a {@link Schema}.</li>
 * </ol>
 * Neither varies per input document, so both are computed once and reused. Initialization is lazy and thread-safe via
 * the initialization-on-demand holder idiom, so concurrent reads (e.g. parallel tests) share the same cached values
 * without locking on the hot path. The scanned map and the compiled {@link Schema} are immutable/thread-safe; only the
 * cheap per-use {@code validator} stays per-call (created by the caller).
 */
final class SchemaCache {

    private static final Logger log = getLogger(SchemaCache.class);

    private SchemaCache() {
    }

    /** Holds the result of the one-time classpath scan for bundled {@code *.xsd.xml} schema resources. */
    private static final class MapHolder {
        static final IOException ERROR;
        static final Map<String, String> MAP;

        static {
            Map<String, String> map = Map.of();
            IOException error = null;
            try {
                map = scanSchemaFiles();
            } catch (IOException | RuntimeException e) {
                error = e instanceof IOException io ? io : new IOException(e);
            }
            MAP = map;
            ERROR = error;
        }
    }

    /** Holds the one-time-compiled GraphML 1.1 schema. */
    private static final class SchemaHolder {
        static final SAXException ERROR;
        static final Schema SCHEMA;

        static {
            Schema schema = null;
            SAXException error = null;
            try {
                schema = compileGraphmlSchema();
            } catch (SAXException e) {
                error = e;
            }
            SCHEMA = schema;
            ERROR = error;
        }
    }

    /**
     * The scanned schema map (schema file name -> content). Scanned once per JVM and reused. The returned map is
     * unmodifiable and safe to share across threads.
     */
    static Map<String, String> externalSchemaMap() throws IOException {
        if (MapHolder.ERROR != null) {
            throw MapHolder.ERROR;
        }
        return MapHolder.MAP;
    }

    /**
     * The compiled GraphML 1.1 schema. Compiled once per JVM and reused. {@link Schema} is immutable and thread-safe;
     * callers must obtain a fresh (non-thread-safe) validator via {@link Schema#newValidator()} per use.
     */
    static Schema graphmlSchema() throws SAXException, IOException {
        // ensure the underlying scan succeeded (the compiled schema depends on it)
        externalSchemaMap();
        if (SchemaHolder.ERROR != null) {
            throw SchemaHolder.ERROR;
        }
        return SchemaHolder.SCHEMA;
    }

    private static Map<String, String> scanSchemaFiles() throws IOException {
        Map<String, String> result = new HashMap<>();
        ClassGraph cg = new ClassGraph();
        try (ResourceList list = cg.scan().getAllResources().filter(r -> r.getPath().contains("schema") && r.getPath().toLowerCase().endsWith(".xsd.xml"))) {
            for (Resource resource : list) {
                String content = resource.getContentAsString();
                int lastSlash = resource.getPath().lastIndexOf('/');
                String schemaName = resource.getPath().substring(lastSlash + 1);
                result.put(schemaName, content);
            }
        }
        return Map.copyOf(result);
    }

    private static Schema compileGraphmlSchema() throws SAXException {
        Map<String, String> schemaMap = MapHolder.MAP;
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        schemaFactory.setResourceResolver((type, namespaceURI, publicId, systemId, baseURI) -> {
            log.info("Requesting resource: type=" + type + ", namespaceURI=" + namespaceURI + ", publicId=" + publicId + ", systemId=" + systemId + ", baseURI=" + baseURI);
            String content = schemaMap.get(systemId);
            if (content == null) {
                log.warn("Schema resource not found for systemId: {}", systemId);
                return null;
            }
            return new SchemaInfo(content, null, null, systemId);
        });
        String graphmlSchema = schemaMap.get("graphml.xsd.xml");
        Source source = new StreamSource(new StringReader(graphmlSchema));
        return schemaFactory.newSchema(source);
    }

}
