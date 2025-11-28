package com.graphinout.reader.gml;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class GmlTokenizer {

    private static final Logger log = getLogger(GmlTokenizer.class);
    private final StreamTokenizer tokenizer;
    private final IGmlHandler handler;

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
                log.warn("Unhandled token " + tokenizer.ttype);
            }
        }
    }

}
