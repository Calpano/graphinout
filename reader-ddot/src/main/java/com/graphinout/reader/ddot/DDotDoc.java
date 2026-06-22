package com.graphinout.reader.ddot;

import java.util.ArrayList;
import java.util.List;

public class DDotDoc {

    public static final String SEPARATOR = " .. ";

    /** Per-link metadata separator (see https://ddot.it): "Meta-data can be appended behind ,,". */
    public static final String META_SEPARATOR = ",,";

    public static class DDotTriple {
        final String subject;
        final String predicate;
        final String object;
        /**
         * Per-link metadata payloads, each rendered inline after a {@code ,,}. A payload is either a
         * structured property {@code ..key.. value} or a free-text note.
         */
        final List<String> meta;

        public DDotTriple(String subject, String predicate, String object) {
            this(subject, predicate, object, new ArrayList<>());
        }

        public DDotTriple(String subject, String predicate, String object, List<String> meta) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
            this.meta = meta;
        }
    }

    final List<DDotTriple> triples = new ArrayList<>();

    public String toDDot() {
        StringBuilder b = new StringBuilder();
        String previousSubject = null;
        for (DDotTriple t : triples) {
            // head: full triple, or continuation form when the subject repeats
            if (t.subject.equals(previousSubject)) {
                b.append(SEPARATOR.stripLeading()).append(t.predicate).append(SEPARATOR);
            } else {
                b.append(t.subject).append(SEPARATOR).append(t.predicate).append(SEPARATOR);
            }
            if (t.object.indexOf('\n') >= 0) {
                // a multi-line value is written as a ddot.it/block literal; its lines follow until the next
                // statement terminates the block (see https://ddot.it/block). Block triples carry no ,, meta.
                b.append(DDotOutput.OBJECT_BLOCK).append('\n');
                for (String blockLine : t.object.split("\n", -1)) {
                    b.append(blockLine).append('\n');
                }
            } else {
                b.append(t.object);
                for (String payload : t.meta) {
                    b.append(' ').append(META_SEPARATOR).append(' ').append(payload);
                }
                b.append('\n');
            }
            previousSubject = t.subject;
        }
        return b.toString();
    }
}
