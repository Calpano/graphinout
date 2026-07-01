package com.graphinout.devtools;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Enforces that no {@code logback*.xml} in the repository references a {@code com.graphinout} package/class that
 * does not exist — so a stale/misspelled {@code <logger name>} fails the build instead of silently doing nothing.
 */
class LogbackLoggerCheckTest {

    @Test
    void everyLoggerNameReferencesARealPackage() throws Exception {
        Path root = LogbackLoggerCheck.findRepoRoot(Path.of("").toAbsolutePath());
        List<LogbackLoggerCheck.Problem> problems = LogbackLoggerCheck.check(root);
        assertTrue(problems.isEmpty(),
                "Stale logback <logger name> entries (no matching package/class in the source tree):\n"
                        + problems.stream()
                        .map(p -> "  " + root.relativize(p.file()) + "  ->  " + p.loggerName())
                        .collect(Collectors.joining("\n")));
    }
}
