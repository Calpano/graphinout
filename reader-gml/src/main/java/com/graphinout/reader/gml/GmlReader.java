package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

public class GmlReader implements GioReader {

    public static final String FORMAT_ID = "gml";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Graph Modelling Language", ".gml");
    private static final Logger log = LoggerFactory.getLogger(GmlReader.class);
    private static final String DELIMITER_REGEX = "\\s+";
    private static final String SECTION_MARKER = "#";
    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parseGmlToCjDocument(SingleInputSource inputSource) throws IOException {
        GmlReader gmlReader = new GmlReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2cj = new CjStream2CjWriter(cj2document);
        gmlReader.read(inputSource, cjStream2cj);
        return cj2document.resultDoc();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            // Emit empty document (no graphs) for empty TGF content to allow exact empty roundtrip
            writer.document(writer.createDocumentChunk());
            return;
        }
        try (Scanner scanner = new Scanner(content)) {
            processFileContent(scanner, writer);
        }
    }


    private void processFileContent(final Scanner scanner, final ICjStream writer) throws IOException {
        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.equalsIgnoreCase("graph")) {
                // Skip the opening bracket
                if (scanner.hasNext() && scanner.next().equals("[")) {
                    parseGraph(scanner, writer);
                }
            }
        }

        writer.graphEnd();
        writer.documentEnd();
    }

    private void parseGraph(Scanner scanner, ICjStream writer) throws IOException {
        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.equalsIgnoreCase("node")) {
                if (scanner.hasNext() && scanner.next().equals("[")) {
                    parseNode(scanner, writer);
                }
            } else if (token.equalsIgnoreCase("edge")) {
                if (scanner.hasNext() && scanner.next().equals("[")) {
                    parseEdge(scanner, writer);
                }
            } else if (token.equals("]")) {
                return;
            }
        }
    }

    private void parseNode(Scanner scanner, ICjStream writer) throws IOException {
        ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
        String id = null;
        String label = null;

        while (scanner.hasNext()) {
            String key = scanner.next();
            if (key.equals("]")) {
                if (id != null) {
                    nodeChunk.id(id);
                }
                if (label != null) {
                    nodeChunk.descriptionPlainText(writer.jsonFactory(), label);
                }
                writer.node(nodeChunk);
                return;
            }

            String value = scanner.next();
            if (key.equalsIgnoreCase("id")) {
                id = value;
            } else if (key.equalsIgnoreCase("label")) {
                // GML labels can be quoted
                if (value.startsWith("\"")) {
                    StringBuilder quotedLabel = new StringBuilder(value.substring(1));
                    while (scanner.hasNext() && !value.endsWith("\"")) {
                        value = scanner.next();
                        quotedLabel.append(" ").append(value);
                    }
                    label = quotedLabel.toString().replaceAll("\"", "");
                } else {
                    label = value;
                }
            } else {
                // Skip other attributes for now, including nested blocks
                skipBlock(scanner, value);
            }
        }
    }

    private void parseEdge(Scanner scanner, ICjStream writer) throws IOException {
        ICjEdgeChunkMutable edgeChunk = writer.createEdgeChunk();
        String source = null;
        String target = null;
        String label = null;

        while (scanner.hasNext()) {
            String key = scanner.next();
            if (key.equals("]")) {
                if (source != null && target != null) {
                    final String finalSource = source;
                    final String finalTarget = target;
                    edgeChunk.addEndpoint(ep -> ep.node(finalSource).direction(CjDirection.OUT));
                    edgeChunk.addEndpoint(ep -> ep.node(finalTarget).direction(CjDirection.IN));
                    if (label != null) {
                        edgeChunk.descriptionPlainText(writer.jsonFactory(), label);
                    }
                    writer.edge(edgeChunk);
                }
                return;
            }

            String value = scanner.next();
            if (key.equalsIgnoreCase("source")) {
                source = value;
            } else if (key.equalsIgnoreCase("target")) {
                target = value;
            } else if (key.equalsIgnoreCase("label")) {
                 if (value.startsWith("\"")) {
                    StringBuilder quotedLabel = new StringBuilder(value.substring(1));
                    while (scanner.hasNext() && !value.endsWith("\"")) {
                        value = scanner.next();
                        quotedLabel.append(" ").append(value);
                    }
                    label = quotedLabel.toString().replaceAll("\"", "");
                } else {
                    label = value;
                }
            } else {
                // Skip other attributes for now
                skipBlock(scanner, value);
            }
        }
    }

    private void skipBlock(Scanner scanner, String value) {
        if (value.equals("[")) {
            int bracketCount = 1;
            while (scanner.hasNext() && bracketCount > 0) {
                String token = scanner.next();
                if (token.equals("[")) {
                    bracketCount++;
                } else if (token.equals("]")) {
                    bracketCount--;
                }
            }
        }
    }
}
