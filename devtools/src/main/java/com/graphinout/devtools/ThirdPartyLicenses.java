package com.graphinout.devtools;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Regenerates the dependency table in {@code THIRD_PARTY.adoc} and the license texts under {@code LICENSES/} from the
 * output of {@code mvn license:aggregate-add-third-party}.
 * <p>
 * Existing rows keep their curated library link, license label and "Original" link. The license text for a new
 * version is taken from the artifact's own jar ({@code META-INF/LICENSE*}, {@code NOTICE*}) when it ships one;
 * otherwise the previous version's folder is carried over. Folders no row references any more are deleted.
 * "Direct" marks every artifact declared in a module POM's own {@code <dependencies>}.
 * <p>
 * Usage (from the repository root):
 * <pre>
 * mvn install -DskipTests
 * mvn org.codehaus.mojo:license-maven-plugin:aggregate-add-third-party
 * mvn -pl devtools -o compile
 * java -cp devtools/target/classes com.graphinout.devtools.ThirdPartyLicenses [--dry-run] [repoRoot]
 * </pre>
 * Exits non-zero if a dependency has no license text or no recognizable license, so it can be fixed by hand.
 */
public class ThirdPartyLicenses {

    /** One line of the license plugin's THIRD-PARTY.txt. */
    record Dep(List<String> licenses, String name, String groupId, String artifactId, String version, String url) {

        String ga() {
            return groupId + ":" + artifactId;
        }

        String folder() {
            return artifactId + "-" + version;
        }
    }

    /** One existing row of the THIRD_PARTY.adoc table. */
    record Row(String lib, String groupId, String artifactId, String version, String dir, String label, String orig) {

        String sortKey() {
            Matcher m = Pattern.compile("^.*?\\[(.*)]$").matcher(lib);
            return (m.matches() ? m.group(1) : lib).toLowerCase(Locale.ROOT);
        }
    }

    static final String ADOC = "THIRD_PARTY.adoc";
    static final String PLUGIN_OUTPUT = "target/generated-sources/license/THIRD-PARTY.txt";
    private static final Pattern ROW = Pattern.compile(
            "^\\| (?<lib>.*?) \\| `(?<g>[^:`]+):(?<a>[^`]+)` \\| `(?<v>[^`]+)` \\| link:LICENSES/(?<dir>[^/]+)/\\[(?<label>[^]]+)] \\| (?<orig>.*?) \\|(?<direct>.*)$",
            Pattern.MULTILINE);
    private static final Pattern COORDS = Pattern.compile("(.*) \\(([^:\\s]+):([^:\\s]+):(\\S+) - (.*)\\)$");
    private static final Pattern LICENSE_FILE = Pattern.compile("^(META-INF/)?(LICENSE|LICENCE|NOTICE|COPYING)[^/]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_BODY = Pattern.compile("(\\| Library \\| Maven coordinates[^\\n]*\\n\\n)(.*?)(\\n\\|===)", Pattern.DOTALL);

    public static void main(String[] args) throws IOException {
        boolean dryRun = List.of(args).contains("--dry-run");
        Path root = Stream.of(args).filter(a -> !a.startsWith("--")).findFirst().map(Path::of)
                .orElse(LogbackLoggerCheck.findRepoRoot(Path.of("").toAbsolutePath()));
        List<String> todo = new ThirdPartyLicenses(root, Path.of(System.getProperty("user.home"), ".m2", "repository"), dryRun).run();
        if (!todo.isEmpty()) {
            System.err.println("Needs manual attention:");
            todo.forEach(t -> System.err.println("  " + t));
            System.exit(1);
        }
    }

    private final Path root;
    private final Path m2;
    private final boolean dryRun;

    ThirdPartyLicenses(Path root, Path m2, boolean dryRun) {
        this.root = root;
        this.m2 = m2;
        this.dryRun = dryRun;
    }

    /** @return problems that need manual attention; empty if everything was resolved */
    List<String> run() throws IOException {
        Path pluginOutput = root.resolve(PLUGIN_OUTPUT);
        if (!Files.exists(pluginOutput)) {
            throw new IOException(pluginOutput + " not found; run mvn org.codehaus.mojo:license-maven-plugin:aggregate-add-third-party first");
        }
        List<Dep> deps = parsePluginOutput(Files.readAllLines(pluginOutput, StandardCharsets.UTF_8)).stream()
                .filter(d -> !d.groupId().equals("com.graphinout")).toList();
        String adoc = Files.readString(root.resolve(ADOC));
        Map<String, Row> old = parseRows(adoc);
        Set<String> direct = directDependencies(root);
        Path licenses = root.resolve("LICENSES");

        List<String> todo = new ArrayList<>();
        List<String> report = new ArrayList<>();
        Set<String> keepDirs = new HashSet<>();
        Map<String, String> rows = new HashMap<>(); // row text -> sort key
        for (Dep d : deps) {
            // exact version first: the plugin may list one artifact in several versions
            Row o = old.getOrDefault(d.ga() + ":" + d.version(), old.get(d.ga()));
            Path dest = licenses.resolve(d.folder());
            String lib = o != null ? o.lib() : d.url() + "[" + d.name() + "]";
            String label = o != null ? o.label() : label(d.licenses());
            String orig;
            String how;
            if (o != null && o.dir().equals(d.folder()) && Files.isDirectory(dest)) {
                orig = o.orig();
                how = "unchanged";
            } else if (o != null && o.orig().contains("raw.githubusercontent.com") && Files.isDirectory(licenses.resolve(o.dir()))) {
                // version-independent upstream text: carry the old folder over
                copyDir(licenses.resolve(o.dir()), dest);
                orig = o.orig();
                how = "copied " + o.dir();
            } else if (extractFromJar(d, dest)) {
                orig = "[link:" + jarUrl(d) + "[1]]";
                how = "from jar";
            } else if (o != null && Files.isDirectory(licenses.resolve(o.dir()))) {
                copyDir(licenses.resolve(o.dir()), dest);
                orig = o.orig();
                how = "copied " + o.dir() + " (jar has no license file)";
            } else {
                orig = "";
                how = "MISSING";
                todo.add("no license text: " + d.ga() + ":" + d.version() + " " + d.licenses());
            }
            if (label.contains("??")) todo.add("unknown license: " + d.ga() + " " + d.licenses());
            keepDirs.add(d.folder());
            String row = ("| " + lib + " | `" + d.ga() + "` | `" + d.version() + "` | link:LICENSES/" + d.folder() + "/[" + label
                    + "] | " + orig + " | " + (direct.contains(d.ga()) ? "✓" : "")).stripTrailing();
            rows.put(row, o != null ? o.sortKey() : d.name().toLowerCase(Locale.ROOT));
            if (o == null) report.add("NEW  " + d.ga() + ":" + d.version() + " [" + label + "] " + how);
            else if (!o.version().equals(d.version())) report.add("VER  " + d.ga() + " " + o.version() + " -> " + d.version() + " " + how);
        }
        Set<String> current = deps.stream().map(Dep::ga).collect(Collectors.toSet());
        old.entrySet().stream().filter(e -> e.getKey().split(":").length == 2).map(Map.Entry::getValue)
                .filter(o -> !current.contains(o.groupId() + ":" + o.artifactId()))
                .forEach(o -> report.add("GONE " + o.groupId() + ":" + o.artifactId() + ":" + o.version()));

        String table = rows.entrySet().stream()
                .sorted(Map.Entry.<String, String>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey).collect(Collectors.joining("\n"));
        Matcher tm = TABLE_BODY.matcher(adoc);
        if (!tm.find()) throw new IOException("dependency table not found in " + ADOC);
        String updated = adoc.substring(0, tm.start()) + tm.group(1) + table + tm.group(3) + adoc.substring(tm.end());

        Set<String> stale = new TreeSet<>();
        try (Stream<Path> s = Files.list(licenses)) {
            s.map(p -> p.getFileName().toString()).filter(n -> !keepDirs.contains(n)).forEach(stale::add);
        }
        Set<String> types = new TreeSet<>();
        rows.keySet().forEach(r -> {
            Matcher m = Pattern.compile("link:LICENSES/[^\\[]+\\[([^]]+)]").matcher(r);
            if (m.find()) types.addAll(List.of(m.group(1).split(" / ")));
        });

        report.forEach(System.out::println);
        System.out.println("removed LICENSES dirs: " + stale);
        System.out.println(types.size() + " license types: " + types + " (check the summary list in " + ADOC + ")");
        if (!dryRun) {
            Files.writeString(root.resolve(ADOC), updated, StandardCharsets.UTF_8);
            for (String s : stale) deleteDir(licenses.resolve(s));
        }
        return todo;
    }

    static List<Dep> parsePluginOutput(List<String> lines) {
        List<Dep> deps = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.strip();
            if (!line.startsWith("(")) continue;
            List<String> licenses = new ArrayList<>();
            int i = 0;
            // leading "(...) (...) " groups; license names may contain nested parentheses
            while (i < line.length() && line.charAt(i) == '(') {
                int depth = 0;
                int j = i;
                for (; j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (c == '(') depth++;
                    else if (c == ')' && --depth == 0) break;
                }
                licenses.add(line.substring(i + 1, j));
                i = j + 1;
                while (i < line.length() && line.charAt(i) == ' ') i++;
            }
            Matcher m = COORDS.matcher(line.substring(i));
            if (!m.matches()) throw new IllegalArgumentException("cannot parse: " + raw);
            deps.add(new Dep(licenses, m.group(1), m.group(2), m.group(3), m.group(4), m.group(5)));
        }
        return deps;
    }

    /** Existing rows, keyed both by {@code groupId:artifactId} and by {@code groupId:artifactId:version}. */
    static Map<String, Row> parseRows(String adoc) {
        Map<String, Row> rows = new HashMap<>();
        Matcher m = ROW.matcher(adoc);
        while (m.find()) {
            Row row = new Row(m.group("lib"), m.group("g"), m.group("a"), m.group("v"), m.group("dir"), m.group("label"), m.group("orig"));
            rows.put(m.group("g") + ":" + m.group("a"), row);
            rows.put(m.group("g") + ":" + m.group("a") + ":" + m.group("v"), row);
        }
        return rows;
    }

    /** Maps a license name as written in a POM to the short SPDX-like label used in THIRD_PARTY.adoc. */
    static String spdx(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        if (n.contains("classpath") || n.contains("cpe") || n.contains("gplv2+ce") || n.contains("gpl2 w/")) return "GPL-2.0+CPE";
        if (n.contains("cddl")) return "CDDL-1.1";
        if (n.contains("lesser") || n.contains("lgpl")) return "LGPL-2.1";
        if (n.contains("mozilla") || n.contains("mpl")) return "MPL-1.1";
        if (n.contains("eclipse public license 1") || n.contains("epl-1") || n.equals("epl 1.0")) return "EPL-1.0";
        if (n.contains("eclipse") || n.contains("epl")) return "EPL-2.0";
        if (n.contains("apache")) return "Apache-2.0";
        if (n.contains("bsd")) return "BSD-3-Clause";
        if (n.contains("mit")) return "MIT";
        return "??" + name;
    }

    static String label(List<String> licenses) {
        Set<String> labels = new LinkedHashSet<>();
        licenses.forEach(l -> labels.add(spdx(l)));
        return String.join(" / ", labels);
    }

    /** {@code groupId:artifactId} of everything declared in a module POM's own {@code <dependencies>}. */
    static Set<String> directDependencies(Path root) throws IOException {
        Set<String> direct = new HashSet<>();
        List<Path> poms = new ArrayList<>();
        poms.add(root.resolve("pom.xml"));
        try (Stream<Path> s = Files.list(root)) {
            s.map(p -> p.resolve("pom.xml")).filter(Files::exists).sorted(Comparator.naturalOrder()).forEach(poms::add);
        }
        Pattern dep = Pattern.compile("<dependency>\\s*<groupId>([^<]+)</groupId>\\s*<artifactId>([^<]+)</artifactId>");
        for (Path pom : poms) {
            String s = Files.readString(pom)
                    .replaceAll("(?s)<dependencyManagement>.*?</dependencyManagement>", "")
                    .replaceAll("(?s)<plugins>.*?</plugins>", "")
                    .replaceAll("(?s)<plugin>.*?</plugin>", "");
            Matcher m = dep.matcher(s);
            while (m.find()) direct.add(m.group(1) + ":" + m.group(2));
        }
        return direct;
    }

    private Path jarPath(Dep d) {
        return m2.resolve(d.groupId().replace('.', '/')).resolve(d.artifactId()).resolve(d.version())
                .resolve(d.artifactId() + "-" + d.version() + ".jar");
    }

    static String jarUrl(Dep d) {
        return "https://repo1.maven.org/maven2/" + d.groupId().replace('.', '/') + "/" + d.artifactId() + "/" + d.version()
                + "/" + d.artifactId() + "-" + d.version() + ".jar";
    }

    private boolean extractFromJar(Dep d, Path dest) throws IOException {
        Path jar = jarPath(d);
        if (!Files.exists(jar)) return false;
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            List<? extends ZipEntry> entries = zip.stream().filter(e -> LICENSE_FILE.matcher(e.getName()).matches()).toList();
            if (entries.isEmpty()) return false;
            if (dryRun) return true;
            Files.createDirectories(dest);
            for (ZipEntry e : entries) {
                String fileName = Path.of(e.getName()).getFileName().toString();
                try (InputStream in = zip.getInputStream(e)) {
                    Files.copy(in, dest.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return true;
    }

    private void copyDir(Path from, Path to) throws IOException {
        if (dryRun) return;
        Files.createDirectories(to);
        try (Stream<Path> s = Files.list(from)) {
            for (Path f : s.toList()) Files.copy(f, to.resolve(f.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteDir(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
