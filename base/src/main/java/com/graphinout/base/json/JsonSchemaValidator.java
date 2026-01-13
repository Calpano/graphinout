package com.graphinout.base.json;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.regex.JoniRegularExpressionFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class JsonSchemaValidator {

    public record JsonSchemaDef(String id, @Nullable String resourcePath, String url, @Nullable String fetchUrl) {}

    public static final JsonSchemaDef CJ = new JsonSchemaDef("https://j-s-o-n.org/schema/connected-json/5.0.0", "/schema/cj/cj-schema.json", "https://calpano.github.io/connected-json/_attachments/cj-schema.json", "https://github.com/Calpano/connected-json/blob/main/modules/ROOT/attachments/cj-schema.json");

    public static final JsonSchemaDef OCIF_06 = new JsonSchemaDef("https://json-schema.org/draft/2020-12/schema", "/ocif-schema-v0.6.json", "https://json-schema.org/draft/2020-12/schema", "https://raw.githubusercontent.com/ocwg/spec/refs/heads/main/spec/v0.6/schema.json");
    private static final Logger log = getLogger(JsonSchemaValidator.class);

    public static boolean isValid(String json, JsonSchemaDef schemaDef, @Nullable Consumer<ContentError> errorConsumer) {
        // This creates a schema factory that will use Draft 2020-12 as the default if $schema is not specified
        // in the schema data. If $schema is specified in the schema data then that schema dialect will be used
        // instead and this version is ignored.
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012, builder ->
                // This creates a mapping from $id which starts with https://www.example.org/ to the retrieval URI classpath:schema/
                builder.schemaMappers(schemaMappers -> {
                    for (JsonSchemaDef jsonSchema : List.of(CJ, OCIF_06)) {
                        if (jsonSchema.resourcePath != null)
                            schemaMappers.mapPrefix(jsonSchema.id, "classpath:" + jsonSchema.resourcePath);
                    }
                }));

        SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
        // By default the JDK regular expression implementation which is not ECMA 262 compliant is used
        // Note that setting this requires including optional dependencies
        // builder.regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
        builder.regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
        SchemaValidatorsConfig config = builder.build();

        // Due to the mapping the schema will be retrieved from the classpath at classpath:schema/example-main.json.
        // If the schema data does not specify an $id the absolute IRI of the schema location will be used as the $id.
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(schemaDef.url()), config);

        try {
            Set<ValidationMessage> assertions = schema.validate(json, InputFormat.JSON, executionContext -> {
                // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
                executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
            });
            if (!assertions.isEmpty()) {
                if (errorConsumer != null) {
                    assertions.stream().map(a -> {
// {
//      "valid": false,
//      "evaluationPath": "/properties/foo/allOf/0",
//      "schemaLocation": "https://json-schema.org/schemas/example#/properties/foo/allOf/0",
//      "instanceLocation": "/foo",
//      "errors": {
//        "required": "Required properties [\"unspecified-prop\"] were not present"
//      }
                        String msg = a.getMessage() + " in path '" + a.getEvaluationPath().toString() + "'";
                        return ContentError.of(ContentError.ErrorLevel.Error, msg, Location.UNAVAILABLE);
                    }).forEach(errorConsumer);
                } else {
                    // FIXME too verbose & insecure?
                    log.warn("Failed to validate:\n----\n" + json + "\n----\n");
                    for (ValidationMessage assertion : assertions) {
                        System.err.println(assertion);
                    }
                }
            }

            return assertions.isEmpty();
        } catch (Throwable t) {
            throw new RuntimeException("while validating JSON", t);
        }

    }

    public static boolean isValidCj(String cjJson) {
        return isValid(cjJson, CJ, null);
    }

    public static boolean isValidCj(String cjJson, @NonNull Consumer<ContentError> errorConsumer) {
        return isValid(cjJson, CJ, errorConsumer);
    }

    public static boolean isValidOCif(String ocifJson) {
        return isValid(ocifJson, OCIF_06, null);
    }

}
