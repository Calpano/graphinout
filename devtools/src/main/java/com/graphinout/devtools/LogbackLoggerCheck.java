package com.graphinout.devtools;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Dev tool: verify that every {@code <logger name="com.graphinout...">} in every {@code logback*.xml} in the
 * repository refers to a package or class that actually exists in the source tree.
 *
 * <p>Logback silently ignores a {@code <logger>} whose name matches nothing, so a renamed or misspelled package
 * (e.g. {@code ...validation.graphml.graphml.ValidatingGraphMlWriter} instead of {@code ...validation.Validating...})
 * becomes dead config that quietly stops working. This tool flags those.
 *
 * <p>Only our own {@code com.graphinout} logger names are validated (they are verifiable against our sources).
 * Third-party names (e.g. {@code org.apache.jena}) cannot be checked against our tree and are reported, by
 * {@link #main}, as unverified.
 *
 * <p>Run from the repo root: {@code java -cp devtools/target/classes com.graphinout.devtools.LogbackLoggerCheck}
 * (or pass the repo root as {@code args[0]}). Enforced by {@code LogbackLoggerCheckTest}.
 */
public final class LogbackLoggerCheck {

    /** A logback config declares a logger name that matches no real package/class in the source tree. */
    public record Problem(Path file, String loggerName, String reason) {
    }

    private static final String OWN_PREFIX = "com.graphinout";

    private LogbackLoggerCheck() {
    }

    public static void main(String[] args) throws IOException {
        Path root = args.length > 0 ? Path.of(args[0]).toAbsolutePath() : findRepoRoot(Path.of("").toAbsolutePath());
        List<Path> configs = findLogbackConfigs(root);
        Set<String> known = collectKnownNamespaces(root);
        List<Problem> problems = check(configs, known);

        System.out.println("Checked " + configs.size() + " logback config(s) against " + known.size()
                + " packages/classes under " + root);
        for (Path cfg : configs) {
            for (String name : loggerNames(cfg)) {
                if (!name.startsWith(OWN_PREFIX)) {
                    System.out.println("  external (unchecked): " + rel(root, cfg) + "  ->  " + name);
                }
            }
        }
        for (Problem p : problems) {
            System.out.println("  STALE: " + rel(root, p.file()) + "  ->  \"" + p.loggerName() + "\"  (" + p.reason() + ")");
        }
        System.out.println(problems.isEmpty()
                ? "OK: all " + OWN_PREFIX + " logger names reference real packages/classes."
                : problems.size() + " stale logger name(s) found.");
        if (!problems.isEmpty()) {
            System.exit(1);
        }
    }

    /** Locate the logback configs and source namespaces under {@code repoRoot}, then check them. */
    public static List<Problem> check(Path repoRoot) throws IOException {
        return check(findLogbackConfigs(repoRoot), collectKnownNamespaces(repoRoot));
    }

    /** One {@link Problem} per {@code com.graphinout} logger name that matches no known package/class. */
    public static List<Problem> check(List<Path> configs, Set<String> knownNamespaces) throws IOException {
        List<Problem> problems = new ArrayList<>();
        for (Path cfg : configs) {
            for (String name : loggerNames(cfg)) {
                if (name.startsWith(OWN_PREFIX) && !isKnown(name, knownNamespaces)) {
                    problems.add(new Problem(cfg, name, "no matching package or class in the source tree"));
                }
            }
        }
        return problems;
    }

    /** A name is valid if it is, or is a parent package of, some known package/class. */
    static boolean isKnown(String name, Set<String> known) {
        if (known.contains(name)) {
            return true;
        }
        String prefix = name + ".";
        for (String k : known) {
            if (k.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /** All package names and fully-qualified top-level class names declared in {@code *.java} files under {@code src}. */
    static Set<String> collectKnownNamespaces(Path root) throws IOException {
        Set<String> known = new HashSet<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> containsDir(p, "src"))
                    .filter(p -> !containsDir(p, "target"))
                    .forEach(java -> {
                        String pkg = readPackage(java);
                        if (pkg == null) {
                            return;
                        }
                        known.add(pkg);
                        String file = java.getFileName().toString();
                        known.add(pkg + "." + file.substring(0, file.length() - ".java".length()));
                    });
        }
        return known;
    }

    static List<Path> findLogbackConfigs(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString();
                        return n.startsWith("logback") && n.endsWith(".xml");
                    })
                    .filter(p -> !containsDir(p, "target"))
                    .sorted()
                    .toList();
        }
    }

    static List<String> loggerNames(Path xml) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document doc = factory.newDocumentBuilder().parse(xml.toFile());
            NodeList loggers = doc.getElementsByTagName("logger");
            List<String> names = new ArrayList<>();
            for (int i = 0; i < loggers.getLength(); i++) {
                Node attr = loggers.item(i).getAttributes().getNamedItem("name");
                if (attr != null) {
                    names.add(attr.getNodeValue());
                }
            }
            return names;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to parse " + xml, e);
        }
    }

    /** The declared package of a Java file, or {@code null} if it has none (default package / unreadable). */
    static String readPackage(Path java) {
        try {
            for (String line : Files.readAllLines(java, StandardCharsets.UTF_8)) {
                String t = line.strip();
                if (t.startsWith("package ") && t.endsWith(";")) {
                    return t.substring("package ".length(), t.length() - 1).strip();
                }
                if (t.startsWith("import ") || t.startsWith("class ") || t.startsWith("public ")
                        || t.startsWith("interface ") || t.startsWith("enum ") || t.startsWith("record ")) {
                    return null;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }

    static boolean containsDir(Path p, String dirName) {
        for (Path part : p) {
            if (part.toString().equals(dirName)) {
                return true;
            }
        }
        return false;
    }

    /** Walk up from {@code start} to the first directory whose {@code pom.xml} is the reactor aggregator. */
    static Path findRepoRoot(Path start) throws IOException {
        for (Path dir = start.toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path pom = dir.resolve("pom.xml");
            if (Files.exists(pom) && Files.readString(pom).contains("<modules>")) {
                return dir;
            }
        }
        return start.toAbsolutePath();
    }

    static String rel(Path root, Path p) {
        try {
            return root.relativize(p).toString();
        } catch (RuntimeException e) {
            return p.toString();
        }
    }
}
