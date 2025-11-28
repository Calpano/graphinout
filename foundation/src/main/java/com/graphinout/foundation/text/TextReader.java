package com.graphinout.foundation.text;

public class TextReader {

    @FunctionalInterface
    public interface ITranscoder {

        /**
         * @param cp     to transform
         * @param nextCp look-ahead, is -1 if end of string
         * @return resulting codepoint from transforming cp
         */
        int cp(int cp, int nextCp);

    }

    @FunctionalInterface
    public interface ICodepointWithLookAhead {

        /**
         * @param cp     to transform
         * @param nextCp look-ahead, is -1 if end of string
         */
        void cp(int cp, int nextCp);

    }

    @FunctionalInterface
    public interface ICodepointWithIndexAndLookAhead {

        /**
         * @param cp     to transform
         * @param nextCp look-ahead, is -1 if end of string
         */
        void cp(int cp, int index, int nextCp);

    }

    /**
     * @param cp_index_nextCp (codepoint, index, next codepoint or -1)
     */
    public static void forEachCodepointWithIndexAndLookAhead(String s, ICodepointWithIndexAndLookAhead cp_index_nextCp) {
        int i = 0;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            int nextCp = i < s.length() ? s.codePointAt(i) : -1;
            cp_index_nextCp.cp(cp, i, nextCp);
        }
    }

    public static void forEachCodepointWithLookAhead(String s, ICodepointWithLookAhead codepointWithLookAhead) {
        forEachCodepointWithIndexAndLookAhead(s, (cp, index, nextCp) -> codepointWithLookAhead.cp(cp, nextCp));
    }

    /**
     * Normalize line breaks (CR LF -> LF, single CR -> LF)
     *
     * @param s to parse
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static void read(String s, ITextWriter textWriter) {
        StringBuilder buf = new StringBuilder();
        Runnable line = () -> {
            textWriter.line(buf.toString());
            buf.setLength(0);
        };
        forEachCodepointWithLookAhead(s, (cp, cpNext) -> {
            switch (cp) {
                // LF -> LF
                case '\n' -> line.run();
                // CR ..
                case '\r' -> {
                    if (cpNext == '\n') {
                        // CR LF => LF
                        // skip this CR, next will be LF
                    } else {
                        // CR => LF
                        line.run();
                    }
                }
                default -> buf.appendCodePoint(cp);
            }
        });
        if(!buf.isEmpty()) {
            line.run();
        }
    }

}
