package com.graphinout.reader.ddot;

import java.util.ArrayList;
import java.util.List;

public class DDotDoc {

    public static final String SEPARATOR = " .. ";

    public static class DDotTriple {
        final String subject;
        final String predicate;
        final String object;

        public DDotTriple(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }
    }

    final List<DDotTriple> triples = new ArrayList<>();

    public String toDDot() {
        StringBuilder b = new StringBuilder();
        String previousSubject = null;
        for (DDotTriple t : triples) {
            if (t.subject.equals(previousSubject)) {
                b.append(SEPARATOR.stripLeading()).append(t.predicate).append(SEPARATOR).append(t.object);
            } else {
                b.append(t.subject).append(SEPARATOR).append(t.predicate).append(SEPARATOR).append(t.object);
            }
            b.append('\n');
            previousSubject = t.subject;
        }
        return b.toString();
    }
}
