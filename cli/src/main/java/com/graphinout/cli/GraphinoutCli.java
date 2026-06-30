package com.graphinout.cli;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.analyze.CjMetaGraph;
import com.graphinout.base.cj.analyze.CjMetaGraphCollector;
import com.graphinout.base.cj.anonymize.AnonymizingCjStream;
import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioInputAnalysis;
import com.graphinout.base.gio.GioInputAnalysisJson;
import com.graphinout.base.gio.GioInputAnalyzer;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.FileSingleInputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.FileOutputSink;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.base.output.OutputSink;
import com.graphinout.engine.GioEngineCore;
import com.graphinout.foundation.pure.input.ContentError;

import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Command-line entry point for Graphinout.
 *
 * <p>Reads a graph file in one of the supported input formats and converts it to one of the supported
 * output formats (via the engine's CJ pipeline). All readers and writers are discovered at runtime through
 * the {@link com.graphinout.base.gio.GioService} {@link java.util.ServiceLoader}, so the available formats
 * reflect whichever reader modules are on the classpath.
 *
 * <pre>
 *   gio formats
 *   gio convert input.gml --to graphml --output out.graphml.xml
 *   gio convert input.tgf --to connected-json        # writes to stdout
 * </pre>
 */
public final class GraphinoutCli {

    /** Default output format used by {@code convert} when {@code --to} is omitted. */
    static final String DEFAULT_OUTPUT_FORMAT = "graphml";

    private static final String PROGRAM = "gio";

    private final GioEngineCore engine;
    private final PrintStream out;
    private final PrintStream err;

    GraphinoutCli(GioEngineCore engine, PrintStream out, PrintStream err) {
        this.engine = engine;
        this.out = out;
        this.err = err;
    }

    public static void main(String[] args) {
        int exitCode = new GraphinoutCli(new GioEngineCore(), System.out, System.err).run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * Execute one CLI invocation.
     *
     * @return process exit code: 0 on success, non-zero on usage or conversion errors.
     */
    int run(String[] args) {
        if (args.length == 0) {
            printUsage(err);
            return 2;
        }
        String command = args[0];
        String[] rest = new String[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);

        switch (command) {
            case "help":
            case "-h":
            case "--help":
                printUsage(out);
                return 0;
            case "version":
            case "-V":
            case "--version":
                out.println(PROGRAM + " " + version());
                return 0;
            case "formats":
            case "list":
                return cmdFormats();
            case "convert":
                return cmdConvert(rest);
            case "analyze":
            case "inspect":
                return cmdAnalyze(rest);
            case "meta":
                return cmdMeta(rest);
            case "detect":
                return cmdDetect(rest);
            default:
                err.println("Unknown command: '" + command + "'");
                printUsage(err);
                return 2;
        }
    }

    // ---------------------------------------------------------------------- formats

    private int cmdFormats() {
        out.println("Input formats (readers):");
        engine.readers().stream()
                .map(GioReader::fileFormat)
                .sorted(Comparator.comparing(GioFileFormat::id))
                .forEach(f -> out.println("  " + formatLine(f)));

        out.println();
        out.println("Output formats (writers):");
        engine.writers().stream()
                .map(GioWriter::fileFormat)
                .sorted(Comparator.comparing(GioFileFormat::id))
                .forEach(f -> out.println("  " + formatLine(f)));
        return 0;
    }

    private static String formatLine(GioFileFormat f) {
        return String.format("%-18s %-26s %s", f.id(), f.label(), String.join(" ", f.fileExtensions()));
    }

    // ---------------------------------------------------------------------- convert

    private int cmdConvert(String[] args) {
        @Nullable String inputPath = null;
        @Nullable String outputPath = null;
        @Nullable String fromFormat = null;
        String toFormat = DEFAULT_OUTPUT_FORMAT;
        boolean anonymize = false;
        boolean meta = false;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-a":
                case "--anonymize":
                    anonymize = true;
                    break;
                case "-m":
                case "--meta":
                    meta = true;
                    break;
                case "-o":
                case "--output":
                    if (++i >= args.length) return missingValue(a);
                    outputPath = args[i];
                    break;
                case "-t":
                case "--to":
                    if (++i >= args.length) return missingValue(a);
                    toFormat = args[i];
                    break;
                case "-f":
                case "--from":
                    if (++i >= args.length) return missingValue(a);
                    fromFormat = args[i];
                    break;
                default:
                    if (a.startsWith("-")) {
                        err.println("Unknown option: '" + a + "'");
                        return 2;
                    }
                    if (inputPath != null) {
                        err.println("Unexpected extra argument: '" + a + "'");
                        return 2;
                    }
                    inputPath = a;
            }
        }

        if (inputPath == null) {
            err.println("convert: missing <input> file");
            err.println("Usage: " + PROGRAM + " convert <input> [--from <id>] [--to <id>] [--output <file>]");
            return 2;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.isFile()) {
            err.println("Input file not found: " + inputFile.getPath());
            return 1;
        }

        GioWriter writer = engine.getWriter(toFormat);
        if (writer == null) {
            err.println("No output writer for format '" + toFormat + "'.");
            err.println("Available output formats: " + availableWriterIds());
            return 1;
        }

        try (SingleInputSource inputSource = new FileSingleInputSource(inputFile)) {
            GioReader reader = selectReader(inputSource, fromFormat);
            if (reader == null) {
                if (fromFormat != null) {
                    err.println("No input reader for format '" + fromFormat + "'.");
                    err.println("Available input formats: " + availableReaderIds());
                } else {
                    err.println("Could not detect the input format of " + inputFile.getPath() + ".");
                    err.println("Specify it explicitly with --from <id>. Available input formats: " + availableReaderIds());
                }
                return 1;
            }

            // Report parse and write problems on stderr; they do not necessarily abort the conversion.
            List<ContentError> errors = new ArrayList<>();
            java.util.function.Consumer<ContentError> errorHandler = e -> {
                errors.add(e);
                err.println("[" + e.level + "] " + e.message);
            };
            reader.setContentErrorHandler(errorHandler);
            writer.setContentErrorHandler(errorHandler);

            err.println("Converting " + inputFile.getName()
                    + " (" + reader.fileFormat().id() + " -> " + writer.fileFormat().id() + ")"
                    + (meta ? " [meta]" : "") + (anonymize ? " [anonymized]" : "") + " ...");

            return outputPath == null
                    ? convertToStdout(reader, writer, inputSource, meta, anonymize)
                    : convertToFile(reader, writer, inputSource, new File(outputPath), meta, anonymize);
        } catch (IOException e) {
            err.println("Conversion failed: " + e.getMessage());
            return 1;
        }
    }

    private int convertToStdout(GioReader reader, GioWriter writer, SingleInputSource inputSource,
                                boolean meta, boolean anonymize) throws IOException {
        InMemoryOutputSink sink = new InMemoryOutputSink();
        pipe(reader, inputSource, cjStream(writer, sink, anonymize), meta);
        closeQuietly(sink);
        out.print(sink.getBufferAsUtf8String());
        out.flush();
        return 0;
    }

    private int convertToFile(GioReader reader, GioWriter writer, SingleInputSource inputSource, File outputFile,
                              boolean meta, boolean anonymize) throws IOException {
        File parent = outputFile.getParentFile();
        if (parent != null) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        OutputSink sink = new FileOutputSink(outputFile);
        try {
            pipe(reader, inputSource, cjStream(writer, sink, anonymize), meta);
        } finally {
            closeQuietly(sink);
        }
        err.println("Wrote " + outputFile.getPath());
        return 0;
    }

    /**
     * Drive the read → (maybe meta) → write pipeline. Without {@code meta} the reader streams straight to the output
     * stream; with it, the input is materialized into a CJ document, replaced by its {@link CjMetaGraph meta graph},
     * and that is streamed out. The output stream is already anonymized if requested, so the order is always
     * read → meta → anonymize → write.
     */
    private void pipe(GioReader reader, SingleInputSource inputSource, ICjStream outStream, boolean meta)
            throws IOException {
        if (meta) {
            CjMetaGraphCollector collector = new CjMetaGraphCollector();
            reader.read(inputSource, collector); // streams the input; only the type projection is buffered
            CjDocument2CjStream.toCjStream(collector.build(), outStream);
        } else {
            reader.read(inputSource, outStream);
        }
    }

    /** The writer's CJ stream, optionally wrapped so labels and content are anonymized before writing. */
    private ICjStream cjStream(GioWriter writer, OutputSink sink, boolean anonymize) {
        ICjStream stream = writer.createCjStream(sink);
        return anonymize ? new AnonymizingCjStream(stream) : stream;
    }

    // ---------------------------------------------------------------------- analyze

    /**
     * Inspect a graph file: read it into the CJ model, then report graph/node/edge counts and the
     * {@link com.graphinout.base.cj.analyze.CjFeature features} it uses. Works for any input format (all readers
     * produce CJ). Output goes to stdout as {@code key: value} lines so it is easy to parse.
     */
    private int cmdAnalyze(String[] args) {
        @Nullable String inputPath = null;
        @Nullable String fromFormat = null;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-f":
                case "--from":
                    if (++i >= args.length) return missingValue(a);
                    fromFormat = args[i];
                    break;
                default:
                    if (a.startsWith("-")) {
                        err.println("Unknown option: '" + a + "'");
                        return 2;
                    }
                    if (inputPath != null) {
                        err.println("Unexpected extra argument: '" + a + "'");
                        return 2;
                    }
                    inputPath = a;
            }
        }

        if (inputPath == null) {
            err.println("analyze: missing <input> file");
            err.println("Usage: " + PROGRAM + " analyze <input> [--from <id>]");
            return 2;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.isFile()) {
            err.println("Input file not found: " + inputFile.getPath());
            return 1;
        }

        try (SingleInputSource inputSource = new FileSingleInputSource(inputFile)) {
            GioReader reader = selectReader(inputSource, fromFormat);
            if (reader == null) {
                if (fromFormat != null) {
                    err.println("No input reader for format '" + fromFormat + "'.");
                } else {
                    err.println("Could not detect the input format of " + inputFile.getPath()
                            + ". Specify it explicitly with --from <id>.");
                }
                err.println("Available input formats: " + availableReaderIds());
                return 1;
            }

            reader.setContentErrorHandler(e -> err.println("[" + e.level + "] " + e.message));

            CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
            reader.read(inputSource, new CjStream2CjWriter(docWriter, true));
            ICjDocument doc = docWriter.resultDoc();
            CjAnalysis analysis = CjAnalyzer.analyze(doc);

            out.println("format:   " + reader.fileFormat().id());
            out.println("graphs:   " + analysis.graphCount());
            out.println("nodes:    " + analysis.nodeCount());
            out.println("edges:    " + analysis.edgeCount());
            out.println("features: " + String.join(", ", analysis.featureSlugs()));
            out.flush();
            return 0;
        } catch (IOException e) {
            err.println("Analysis failed: " + e.getMessage());
            return 1;
        }
    }

    // ---------------------------------------------------------------------- meta

    /**
     * Infer a meta graph (schema) from any input and emit it as Connected JSON. Every distinct node type and edge type
     * becomes a node (typed {@code Node}/{@code Edge}) carrying its instance {@code count}; node types link to the edge
     * types their instances touch via {@code uses} edges, and strict-subset edge usage is generalised into
     * {@code has subtype} edges (see {@link CjMetaGraph}). Output goes to stdout, or to {@code -o <file>}.
     */
    private int cmdMeta(String[] args) {
        @Nullable String inputPath = null;
        @Nullable String fromFormat = null;
        @Nullable String outputPath = null;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-f":
                case "--from":
                    if (++i >= args.length) return missingValue(a);
                    fromFormat = args[i];
                    break;
                case "-o":
                case "--output":
                    if (++i >= args.length) return missingValue(a);
                    outputPath = args[i];
                    break;
                default:
                    if (a.startsWith("-")) {
                        err.println("Unknown option: '" + a + "'");
                        return 2;
                    }
                    if (inputPath != null) {
                        err.println("Unexpected extra argument: '" + a + "'");
                        return 2;
                    }
                    inputPath = a;
            }
        }

        if (inputPath == null) {
            err.println("meta: missing <input> file");
            err.println("Usage: " + PROGRAM + " meta <input> [--from <id>] [-o <file>]");
            return 2;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.isFile()) {
            err.println("Input file not found: " + inputFile.getPath());
            return 1;
        }

        try (SingleInputSource inputSource = new FileSingleInputSource(inputFile)) {
            GioReader reader = selectReader(inputSource, fromFormat);
            if (reader == null) {
                if (fromFormat != null) {
                    err.println("No input reader for format '" + fromFormat + "'.");
                } else {
                    err.println("Could not detect the input format of " + inputFile.getPath()
                            + ". Specify it explicitly with --from <id>.");
                }
                err.println("Available input formats: " + availableReaderIds());
                return 1;
            }

            reader.setContentErrorHandler(e -> err.println("[" + e.level + "] " + e.message));

            CjMetaGraphCollector collector = new CjMetaGraphCollector();
            reader.read(inputSource, collector); // streams the input; only the type projection is buffered
            String json = CjDocuments.toJsonString(collector.build());
            if (outputPath != null) {
                try (PrintStream fileOut = new PrintStream(new File(outputPath), "UTF-8")) {
                    fileOut.println(json);
                }
            } else {
                out.println(json);
                out.flush();
            }
            return 0;
        } catch (IOException e) {
            err.println("Meta failed: " + e.getMessage());
            return 1;
        }
    }

    // ---------------------------------------------------------------------- detect

    /**
     * Probe every reader against the input and print the ranked-candidate {@link GioInputAnalysis} as JSON
     * (formats, outcomes, parse stats, confidence and the signals behind each score). Unlike {@code analyze}
     * (which inspects a single chosen reader), {@code detect} answers "which format is this?".
     */
    private int cmdDetect(String[] args) {
        @Nullable String inputPath = null;
        for (String a : args) {
            if (a.startsWith("-")) {
                err.println("Unknown option: '" + a + "'");
                return 2;
            }
            if (inputPath != null) {
                err.println("Unexpected extra argument: '" + a + "'");
                return 2;
            }
            inputPath = a;
        }
        if (inputPath == null) {
            err.println("detect: missing <input> file");
            err.println("Usage: " + PROGRAM + " detect <input>");
            return 2;
        }
        File inputFile = new File(inputPath);
        if (!inputFile.isFile()) {
            err.println("Input file not found: " + inputFile.getPath());
            return 1;
        }
        try (SingleInputSource inputSource = new FileSingleInputSource(inputFile)) {
            GioInputAnalysis analysis = new GioInputAnalyzer(engine.readers()).analyze(inputSource);
            out.println(GioInputAnalysisJson.toJson(analysis));
            out.flush();
            return 0;
        } catch (IOException e) {
            err.println("Detection failed: " + e.getMessage());
            return 1;
        }
    }

    // ---------------------------------------------------------------------- reader selection

    /**
     * Pick a reader for the given input. When {@code forcedFormatId} is set, only that reader is used;
     * otherwise readers are filtered by file extension (falling back to all readers) and then probed with
     * {@link GioReader#isValid}. If several candidates remain, the first one is chosen and the rest are
     * reported on stderr.
     */
    @Nullable
    GioReader selectReader(SingleInputSource inputSource, @Nullable String forcedFormatId) {
        if (forcedFormatId != null) {
            return engine.getReader(forcedFormatId);
        }

        List<GioReader> candidates = new ArrayList<>();
        for (GioReader r : engine.readers()) {
            if (r.fileFormat().matches(inputSource.name())) {
                candidates.add(r);
            }
        }
        if (candidates.isEmpty()) {
            candidates.addAll(engine.readers());
        }

        List<GioReader> valid = new ArrayList<>();
        for (GioReader r : candidates) {
            try {
                if (r.isValid(inputSource)) {
                    valid.add(r);
                }
            } catch (Exception e) {
                // not a match; keep probing the remaining candidates
            }
        }
        if (valid.isEmpty()) {
            return null;
        }
        if (valid.size() > 1) {
            StringBuilder others = new StringBuilder();
            for (int i = 1; i < valid.size(); i++) {
                if (i > 1) others.append(", ");
                others.append(valid.get(i).fileFormat().id());
            }
            err.println("Multiple readers accept this input; using '" + valid.get(0).fileFormat().id()
                    + "' (also: " + others + "). Use --from to override.");
        }
        return valid.get(0);
    }

    // ---------------------------------------------------------------------- helpers

    private String availableReaderIds() {
        return engine.readers().stream()
                .map(r -> r.fileFormat().id())
                .distinct().sorted().reduce((a, b) -> a + ", " + b).orElse("(none)");
    }

    private String availableWriterIds() {
        return engine.writers().stream()
                .map(w -> w.fileFormat().id())
                .distinct().sorted().reduce((a, b) -> a + ", " + b).orElse("(none)");
    }

    private int missingValue(String option) {
        err.println("Option '" + option + "' requires a value.");
        return 2;
    }

    private static void closeQuietly(AutoCloseable c) {
        try {
            c.close();
        } catch (Exception ignored) {
            // best effort
        }
    }

    private static String version() {
        String v = GraphinoutCli.class.getPackage().getImplementationVersion();
        return v != null ? v : "(development)";
    }

    private void printUsage(PrintStream s) {
        s.println("Graphinout CLI - convert between graph file formats.");
        s.println();
        s.println("Usage: " + PROGRAM + " <command> [options]");
        s.println();
        s.println("Commands:");
        s.println("  formats                          List supported input and output formats");
        s.println("  convert <input> [options]        Convert a graph file to another format");
        s.println("  analyze <input> [--from <id>]    Inspect a graph: graph/node/edge counts and features used");
        s.println("  meta <input> [--from <id>]       Infer a type schema (node/edge types, uses, subtypes) as CJ");
        s.println("  detect <input>                   Probe all readers; print ranked format candidates as JSON");
        s.println("  version                          Print the version");
        s.println("  help                             Show this help");
        s.println();
        s.println("convert options:");
        s.println("  -t, --to <id>        Output format id (default: " + DEFAULT_OUTPUT_FORMAT + ")");
        s.println("  -o, --output <file>  Write to a file instead of stdout");
        s.println("  -f, --from <id>      Force the input format id (default: auto-detect)");
        s.println("  -m, --meta           Replace the graph with its inferred type schema (see 'meta')");
        s.println("  -a, --anonymize      Redact labels/types/content (letters->X/x, digits->0);");
        s.println("                       remap ids to node1/edge1/…; keep structure, spacing, links");
        s.println("                       Pipeline order: read -> [meta] -> [anonymize] -> write");
        s.println();
        s.println("Examples:");
        s.println("  " + PROGRAM + " formats");
        s.println("  " + PROGRAM + " convert graph.gml --to graphml --output graph.graphml.xml");
        s.println("  " + PROGRAM + " convert graph.tgf --to connected-json");
    }
}
