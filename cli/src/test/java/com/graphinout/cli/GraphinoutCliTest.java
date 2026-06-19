package com.graphinout.cli;

import com.graphinout.engine.GioEngineCore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphinoutCliTest {

    /** A tiny Trivial Graph Format document: two nodes and one edge. */
    private static final String TGF = "1 A\n2 B\n#\n1 2 edge\n";

    private static class Result {
        final int exitCode;
        final String out;
        final String err;

        Result(int exitCode, String out, String err) {
            this.exitCode = exitCode;
            this.out = out;
            this.err = err;
        }
    }

    private Result run(String... args) {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBuf, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errBuf, true, StandardCharsets.UTF_8);
        int code = new GraphinoutCli(new GioEngineCore(), out, err).run(args);
        return new Result(code, outBuf.toString(StandardCharsets.UTF_8), errBuf.toString(StandardCharsets.UTF_8));
    }

    @Test
    void formatsListsReadersAndWriters() {
        Result r = run("formats");
        assertEquals(0, r.exitCode);
        assertTrue(r.out.contains("Input formats"), r.out);
        assertTrue(r.out.contains("Output formats"), r.out);
        assertTrue(r.out.contains("graphml"), r.out);
        assertTrue(r.out.contains("connected-json"), r.out);
    }

    @Test
    void noArgsPrintsUsageAndFails() {
        Result r = run();
        assertEquals(2, r.exitCode);
        assertTrue(r.err.contains("Usage"), r.err);
    }

    @Test
    void convertToStdoutAutoDetectsInputFormat(@TempDir Path dir) throws Exception {
        File in = dir.resolve("graph.tgf").toFile();
        Files.writeString(in.toPath(), TGF);

        Result r = run("convert", in.getPath(), "--to", "connected-json");
        assertEquals(0, r.exitCode, r.err);
        assertTrue(r.out.contains("\"graphs\""), r.out);
        assertTrue(r.out.contains("\"nodes\""), r.out);
    }

    @Test
    void anonymizeRedactsLabelsAndRemapsIds(@TempDir Path dir) throws Exception {
        File in = dir.resolve("graph.tgf").toFile();
        Files.writeString(in.toPath(), TGF); // nodes "A"/"B", edge label "edge"

        Result r = run("convert", in.getPath(), "--to", "connected-json", "--anonymize");
        assertEquals(0, r.exitCode, r.err);
        assertTrue(r.out.contains("\"node1\""), "ids remapped: " + r.out);
        assertTrue(r.out.contains("\"xxxx\""), "edge label 'edge' -> 'xxxx': " + r.out);
        assertFalse(r.out.contains("\"edge\""), "original label text must be gone: " + r.out);
    }

    @Test
    void convertWritesToOutputFile(@TempDir Path dir) throws Exception {
        File in = dir.resolve("graph.tgf").toFile();
        Files.writeString(in.toPath(), TGF);
        File out = dir.resolve("out.graphml.xml").toFile();

        Result r = run("convert", in.getPath(), "--to", "graphml", "--output", out.getPath());
        assertEquals(0, r.exitCode, r.err);
        assertTrue(out.isFile(), "output file should exist");
        String graphml = Files.readString(out.toPath());
        assertTrue(graphml.contains("<graphml"), graphml);
    }

    @Test
    void unknownOutputFormatFails(@TempDir Path dir) throws Exception {
        File in = dir.resolve("graph.tgf").toFile();
        Files.writeString(in.toPath(), TGF);

        Result r = run("convert", in.getPath(), "--to", "does-not-exist");
        assertEquals(1, r.exitCode);
        assertTrue(r.err.contains("No output writer"), r.err);
    }

    @Test
    void missingInputFileFails() {
        Result r = run("convert", "/no/such/file.tgf");
        assertEquals(1, r.exitCode);
        assertTrue(r.err.contains("not found"), r.err);
    }
}
