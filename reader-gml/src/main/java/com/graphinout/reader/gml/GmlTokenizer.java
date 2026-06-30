package com.graphinout.reader.gml;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class GmlTokenizer {

    private static final Logger log = getLogger(GmlTokenizer.class);
    /** Beyond this many unexpected characters the input clearly is not GML; stop, to avoid flooding errors/logs. */
    private static final int MAX_UNEXPECTED_TOKENS = 50;
    private final StreamTokenizer tokenizer;
    private final IGmlHandler handler;
    private @Nullable Consumer<ContentError> errorHandler;

    public GmlTokenizer(Reader reader, IGmlHandler handler) {
        this.tokenizer = new StreamTokenizer(reader);
        this.handler = handler;
        // Configure tokenizer to avoid numeric parsing so numbers remain strings
        tokenizer.resetSyntax();
        // Whitespace (ASCII control to space)
        tokenizer.whitespaceChars(0, ' ');
        // Words: letters, digits, underscore, dot, minus
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('_', '_');
        tokenizer.wordChars('.', '.');
        tokenizer.wordChars('-', '-');
        // Quoted strings
        tokenizer.quoteChar('"');
        // Brackets as ordinary characters so we can detect list boundaries
        tokenizer.ordinaryChar('[');
        tokenizer.ordinaryChar(']');
        // Line comments starting with '#'
        tokenizer.commentChar('#');
    }

    /** report tokenizer-level content errors (unexpected characters); pass null to ignore */
    public void setContentErrorHandler(@Nullable Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /** @return the current 1-based line number in the input */
    public int currentLine() {
        return tokenizer.lineno();
    }

    public static void tokenize(Reader reader, IGmlHandler gmlHandler) {
        GmlTokenizer tokenizer = new GmlTokenizer(reader, gmlHandler);
        try {
            tokenizer.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Object> tokenizeToList(String string) {
        GmlListHandler handler = new GmlListHandler();
        StringReader stringReader = new StringReader(string);
        tokenize(stringReader, handler);
        return handler.list();
    }

    public void parse() throws IOException {
        int unexpected = 0;
        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                handler.key(tokenizer.sval);

                // Check for a subsequent value
                tokenizer.nextToken();
                if (tokenizer.ttype == StreamTokenizer.TT_WORD || tokenizer.ttype == '"' || tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                    if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                        handler.value(String.valueOf(tokenizer.nval));
                    } else {
                        handler.value(tokenizer.sval);
                    }
                } else {
                    // Not a value, so push the token back to be processed in the next loop iteration
                    tokenizer.pushBack();
                }
            } else if (tokenizer.ttype == '[') {
                handler.open();
            } else if (tokenizer.ttype == ']') {
                handler.close();
            } else {
                // Unexpected character: report it through the content-error handler (the caller decides whether to
                // log) and NEVER log per token — detection probes this reader against every input, so a non-GML file
                // would otherwise flood the log with one WARN per character. Bail once it is clearly not GML.
                if (++unexpected > MAX_UNEXPECTED_TOKENS) {
                    if (errorHandler != null) {
                        errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, //
                                "Too many unexpected characters — input does not look like GML", Location.of(tokenizer.lineno(), 1)));
                    }
                    return;
                }
                if (errorHandler != null) {
                    String shown = tokenizer.ttype >= 0 ? "'" + (char) tokenizer.ttype + "'" : "type " + tokenizer.ttype;
                    errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, //
                            "Unexpected character " + shown + " in GML", Location.of(tokenizer.lineno(), 1)));
                }
            }
        }
    }

}
