package com.calpano.graphinout.base.output;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

@Slf4j
public class TempFileOutputSink implements OutputSink {

    private static Path tempDirectory;

    static {
        try {
            tempDirectory = Files.createTempDirectory(Paths.get("./"), "TMP_GRAPH_");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final File tmpFile;
    private transient OutputStream out;
    private transient Writer w;

    public TempFileOutputSink(String name) {
        try {
            tmpFile = Files.createTempFile(tempDirectory, new Date().toString().replace(" ", "_") + "_" + name + "_", ".tmp").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OutputStream outputStream() throws IOException {
        if (out == null) {
            out = new FileOutputStream(tmpFile, true);
        }
        return out;
    }


    private void delete() {
        if (!Files.exists(tempDirectory)) {
            return;
        }
        try {
            Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    Files.deleteIfExists(dir);
                    return super.postVisitDirectory(dir, exc);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.deleteIfExists(file);
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        log.debug("Closed OutputSink  <{}}> type <{}>.", tmpFile.getName(), out.getClass().getName());
        out.close();
        w.close();
        delete();
    }
}
