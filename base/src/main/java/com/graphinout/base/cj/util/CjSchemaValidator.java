package com.graphinout.base.cj.util;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.ConnectedJson;
import com.graphinout.base.input.InputSource;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.regex.JoniRegularExpressionFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class CjSchemaValidator {


    /**
     * Validate with Connected JSON (CJ) Schema 1.0 Canonical
     *
     * @param inputSource to check
     */
    public static boolean isValidCjCanonical(InputSource inputSource) throws IOException {
        SchemaRegistryConfig.Builder builder = SchemaRegistryConfig.builder();
        // By default the JDK regular expression implementation which is not ECMA 262 compliant is used
        // Note that setting this requires including optional dependencies
        // builder.regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
        builder.regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
        // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
        builder.formatAssertionsEnabled(true);
        SchemaRegistryConfig config = builder.build();

        // This creates a schema registry that will use Draft 2020-12 as the default if $schema is not specified
        // in the schema data. If $schema is specified in the schema data then that schema dialect will be used
        // instead and this version is ignored.
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, registryBuilder ->
                // This creates a mapping from $id which starts with https://www.example.org/ to the retrieval URI classpath:schema/
                registryBuilder.schemaIdResolvers(schemaIdResolvers -> //
                        schemaIdResolvers.mapPrefix(ConnectedJson.CJ_SCHEMA_URL, "classpath:" + CjConstants.SCHEMA_RESOURCE)) //
                        .schemaRegistryConfig(config));

        // Due to the mapping the schema will be retrieved from the classpath at classpath:schema/example-main.json.
        // If the schema data does not specify an $id the absolute IRI of the schema location will be used as the $id.
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of(ConnectedJson.CJ_SCHEMA_URL));

        // read input source to string
        String json = inputSource.asSingle().getContentAsUtf8String();

        try {
            List<Error> assertions = schema.validate(json, InputFormat.JSON);
            if(!assertions.isEmpty()) {
                log.warn("Failed to validate:\n----\n"+json+"\n----\n");
                for (Error assertion : assertions) {
                    System.err.println(assertion);
                }
            }

            return assertions.isEmpty();
        } catch (Throwable t) {
            throw new RuntimeException("while validating "+inputSource.name(),t);
        }

    }

    private static final Logger log = getLogger(CjSchemaValidator.class);

}
