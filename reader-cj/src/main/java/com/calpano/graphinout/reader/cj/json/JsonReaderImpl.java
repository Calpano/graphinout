package com.calpano.graphinout.reader.cj.json;

import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.json.JsonEventStream;
import com.calpano.graphinout.foundation.json.JsonReader;
import com.amazon.ion.IonReader;
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonType;
import com.amazon.ion.system.IonSystemBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class JsonReaderImpl implements JsonReader {

    private static final IonSystem ION_SYSTEM = IonSystemBuilder.standard().build();

    // JSON5 preprocessing patterns
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("//.*$", Pattern.MULTILINE);
    private static final Pattern MULTI_LINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern UNQUOTED_KEY = Pattern.compile("([{,]\\s*)([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*:");
    private static final Pattern TRAILING_COMMA = Pattern.compile(",\\s*([}\\]])");
    private static final Pattern SINGLE_QUOTES = Pattern.compile("'([^'\\\\]*(\\\\.[^'\\\\]*)*)'");
    private static final Pattern PLUS_NUMBERS = Pattern.compile("\\+(?=\\d)");
    private static final Pattern HEX_NUMBERS = Pattern.compile("0[xX]([0-9a-fA-F]+)");
    private static final Pattern LEADING_DECIMAL = Pattern.compile("(?<=[\\[{,:\\s])\\.(?=\\d)");
    private static final Pattern TRAILING_DECIMAL = Pattern.compile("(?<=\\d)\\.(?=[\\s,}\\]])");
    private static final Pattern INFINITY_PATTERN = Pattern.compile("\\bInfinity\\b");
    private static final Pattern NEG_INFINITY_PATTERN = Pattern.compile("\\b-Infinity\\b");
    private static final Pattern NAN_PATTERN = Pattern.compile("\\bNaN\\b");

    @Override
    public void read(InputSource inputSource, JsonEventStream stream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;

        // Read the entire content as string for Ion parsing
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }

        // Preprocess JSON5 content to make it compatible with Ion
        String preprocessedContent = preprocessJson5(content);

        // Create Ion reader for JSON5 parsing
        IonReader reader = ION_SYSTEM.newReader(preprocessedContent);

        try {
            // Advance to the first value and parse it
            if (reader.next() != null) {
                parseIonValue(reader, stream);
            }
        } finally {
            reader.close();
        }
    }

    private void parseIonValue(IonReader reader, JsonEventStream stream) throws IOException {
        IonType type = reader.getType();
        if (reader.isNullValue()) {
            stream.onNull();
            return;
        }

        switch (type) {
            case STRING:
                stream.onString(reader.stringValue());
                break;
            case INT:
                // Handle different integer types
                try {
                    int intValue = reader.intValue();
                    stream.onInteger(intValue);
                } catch (Exception e) {
                    try {
                        long longValue = reader.longValue();
                        stream.onLong(longValue);
                    } catch (Exception e2) {
                        BigInteger bigIntValue = reader.bigIntegerValue();
                        stream.onBigInteger(bigIntValue);
                    }
                }
                break;
            case FLOAT:
                // Handle different float types
                try {
                    double doubleValue = reader.doubleValue();
                    // Check if it's a special value (Infinity, -Infinity, NaN)
                    if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
                        stream.onDouble(doubleValue);
                    } else {
                        // Try to determine if it should be float or double based on precision
                        BigDecimal bigDecimalValue = reader.bigDecimalValue();
                        if (bigDecimalValue.scale() <= 7 && bigDecimalValue.precision() <= 7) {
                            stream.onFloat((float) doubleValue);
                        } else {
                            stream.onDouble(doubleValue);
                        }
                    }
                } catch (Exception e) {
                    BigDecimal bigDecimalValue = reader.bigDecimalValue();
                    stream.onBigDecimal(bigDecimalValue);
                }
                break;
            case BOOL:
                stream.onBoolean(reader.booleanValue());
                break;
            case LIST:
                stream.arrayStart();
                reader.stepIn();
                while (reader.next() != null) {
                    parseIonValue(reader, stream);
                }
                reader.stepOut();
                stream.arrayEnd();
                break;
            case STRUCT:
                stream.objectStart();
                reader.stepIn();
                while (reader.next() != null) {
                    String fieldName = reader.getFieldName();
                    stream.onKey(fieldName);
                    parseIonValue(reader, stream);
                }
                reader.stepOut();
                stream.objectEnd();
                break;
            default:
                // For any other types, try to convert to string as fallback
                stream.onString(reader.stringValue());
                break;
        }
    }

    private String preprocessJson5(String json5Content) {
        String result = json5Content;

        // Remove comments first
        result = SINGLE_LINE_COMMENT.matcher(result).replaceAll("");
        result = MULTI_LINE_COMMENT.matcher(result).replaceAll("");

        // Convert single quotes to double quotes
        result = SINGLE_QUOTES.matcher(result).replaceAll("\"$1\"");

        // Quote unquoted object keys
        result = UNQUOTED_KEY.matcher(result).replaceAll("$1\"$2\":");

        // Remove trailing commas
        result = TRAILING_COMMA.matcher(result).replaceAll("$1");

        // Remove plus signs from numbers
        result = PLUS_NUMBERS.matcher(result).replaceAll("");

        // Convert hex numbers to decimal
        result = HEX_NUMBERS.matcher(result).replaceAll(matchResult -> {
            String hex = matchResult.group(1);
            return String.valueOf(Integer.parseInt(hex, 16));
        });

        // Fix leading decimal points
        result = LEADING_DECIMAL.matcher(result).replaceAll("0.");

        // Fix trailing decimal points
        result = TRAILING_DECIMAL.matcher(result).replaceAll(".0");

        // Handle special number values
        result = INFINITY_PATTERN.matcher(result).replaceAll("1e308");
        result = NEG_INFINITY_PATTERN.matcher(result).replaceAll("-1e308");
        result = NAN_PATTERN.matcher(result).replaceAll("null");

        return result;
    }

}
