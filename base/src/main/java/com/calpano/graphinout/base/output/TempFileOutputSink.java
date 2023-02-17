package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.output.OutputSink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Object> outputInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("type", "tmp file");
        info.put("name", tmpFile.getName());
        info.put("path", tmpFile.getAbsolutePath());
        return info;
    }

    @Override
    public OutputStream outputStream() throws IOException {
        if (out == null) {
            out = new FileOutputStream(tmpFile, true);
        }
        return out;
    }

    @Override
    public List<String> readAllData() throws IOException {
        return Files.readAllLines(tmpFile.toPath());
    }

    public void removeTemp() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                delete();
            }
        });
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
}
