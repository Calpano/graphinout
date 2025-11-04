package com.graphinout.reader.gml;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

import static org.slf4j.LoggerFactory.getLogger;

public class GmlTokenizer {

    private final StreamTokenizer tokenizer;
    private final IGmlHandler handler;

    public GmlTokenizer(Reader reader, IGmlHandler handler) {
        this.tokenizer = new StreamTokenizer(reader);
        this.handler = handler;
        tokenizer.commentChar('#');
        tokenizer.wordChars('_', '_');
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
                log.warn("Unhandled token "+tokenizer.ttype);
            }
        }
    }

    private static final Logger log = getLogger(GmlTokenizer.class);
}
