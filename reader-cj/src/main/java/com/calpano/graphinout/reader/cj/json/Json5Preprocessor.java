package com.calpano.graphinout.reader.cj.json;

import java.util.regex.Pattern;

public class Json5Preprocessor {

    // JSON5 preprocessing patterns
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("//.*$", Pattern.MULTILINE);
    private static final Pattern MULTI_LINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern MULTILINE_STRING = Pattern.compile("\\\\\\r?\\n");
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

    /**
     * Convert all relaxed JSON5 specialties to their stricter JSON versions.
     *
     * @param json5 to convert
     * @return pure JSON
     */
    public static String toJson(String json5) {
        String result = json5;

        // Handle multiline strings first (before comment removal)
        result = MULTILINE_STRING.matcher(result).replaceAll("");

        // Remove comments
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
