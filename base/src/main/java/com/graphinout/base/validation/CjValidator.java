package com.graphinout.base.validation;

import com.graphinout.base.cj.Cj;
import com.graphinout.foundation.input.InputSource;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.regex.JoniRegularExpressionFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class CjValidator {


    /**
     * Validate with Connected JSON (CJ) Schema 1.0 Canonical
     *
     * @param inputSource to check
     */
    public static boolean isValidCjCanonical(InputSource inputSource) throws IOException {
        // This creates a schema factory that will use Draft 2020-12 as the default if $schema is not specified
        // in the schema data. If $schema is specified in the schema data then that schema dialect will be used
        // instead and this version is ignored.
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012, builder ->
                // This creates a mapping from $id which starts with https://www.example.org/ to the retrieval URI classpath:schema/
                builder.schemaMappers(schemaMappers -> schemaMappers.mapPrefix(Cj.SCHEMA_ID, "classpath:" + Cj.SCHEMA_RESOURCE)));

        SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
        // By default the JDK regular expression implementation which is not ECMA 262 compliant is used
        // Note that setting this requires including optional dependencies
        // builder.regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
        builder.regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
        SchemaValidatorsConfig config = builder.build();

        // Due to the mapping the schema will be retrieved from the classpath at classpath:schema/example-main.json.
        // If the schema data does not specify an $id the absolute IRI of the schema location will be used as the $id.
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(Cj.SCHEMA_URL), config);

        // read input source to string
        String json = IOUtils.toString(inputSource.asSingle().inputStream(), StandardCharsets.UTF_8);

        Set<ValidationMessage> assertions = schema.validate(json, InputFormat.JSON, executionContext -> {
            // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });

        if(!assertions.isEmpty()) {
            for (ValidationMessage assertion : assertions) {
                System.err.println(assertion);
            }
        }

        return assertions.isEmpty();
    }

}
