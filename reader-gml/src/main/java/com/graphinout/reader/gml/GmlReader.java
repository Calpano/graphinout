package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.InputSource;
import com.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
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

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
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
                        edgeChunk.addLabelWithoutLanguage(label);
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
                // Capture simple attributes (non-nested) into edge data; skip nested blocks
                if ("[".equals(value)) {
                    skipBlock(scanner, value);
                } else {
                    String attrValue = value;
                    if (attrValue.startsWith("\"")) {
                        StringBuilder quoted = new StringBuilder(attrValue.substring(1));
                        while (scanner.hasNext() && !attrValue.endsWith("\"")) {
                            attrValue = scanner.next();
                            quoted.append(" ").append(attrValue);
                        }
                        attrValue = quoted.toString().replaceAll("\"", "");
                    }
                    final String k = key;
                    final String v = attrValue;
                    edgeChunk.dataMutable(d -> d.addProperty(k, v));
                }
            }
        }
    }

    private void parseGraph(Scanner scanner, ICjStream cjStream) throws IOException {
        ICjGraphChunkMutable graphChunk = cjStream.createGraphChunk();
        boolean started = false;
        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.equalsIgnoreCase("node")) {
                if (scanner.hasNext() && scanner.next().equals("[")) {
                    if (!started) {
                        cjStream.graphStart(graphChunk);
                        started = true;
                    }
                    parseNode(scanner, cjStream);
                }
            } else if (token.equalsIgnoreCase("edge")) {
                if (scanner.hasNext() && scanner.next().equals("[")) {
                    if (!started) {
                        cjStream.graphStart(graphChunk);
                        started = true;
                    }
                    parseEdge(scanner, cjStream);
                }
            } else if (token.equalsIgnoreCase("graph")) {
                // nested graph
                if (scanner.hasNext() && scanner.next().equals("[")) {
                    if (!started) {
                        cjStream.graphStart(graphChunk);
                        started = true;
                    }
                    parseGraph(scanner, cjStream);
                }
            } else if (token.equalsIgnoreCase("name")) {
                String value = scanner.next();
                if (value.startsWith("\"")) {
                    StringBuilder quotedName = new StringBuilder(value.substring(1));
                    while (scanner.hasNext() && !value.endsWith("\"")) {
                        value = scanner.next();
                        quotedName.append(" ").append(value);
                    }
                    graphChunk.addLabelWithoutLanguage(quotedName.toString().replaceAll("\"", ""));
                } else {
                    graphChunk.addLabelWithoutLanguage(value);
                }
            } else if (token.equals("]")) {
                break;
            } else {
                // generic attribute inside graph
                if (!started) {
                    // if we encounter attributes before any child lists, we still need to start the graph later
                }
                if (scanner.hasNext()) {
                    String value = scanner.next();
                    if ("[".equals(value)) {
                        // nested block attribute, skip it
                        skipBlock(scanner, value);
                    } else {
                        String attrValue = value;
                        if (attrValue.startsWith("\"")) {
                            StringBuilder quoted = new StringBuilder(attrValue.substring(1));
                            while (scanner.hasNext() && !attrValue.endsWith("\"")) {
                                attrValue = scanner.next();
                                quoted.append(" ").append(attrValue);
                            }
                            attrValue = quoted.toString().replaceAll("\"", "");
                        }
                        final String key = token;
                        final String val = attrValue;
                        graphChunk.dataMutable(d -> d.addProperty(key, val));
                    }
                }
            }
        }
        if (!started) {
            cjStream.graphStart(graphChunk);
        }
        cjStream.graphEnd();
    }

    private void parseNode(Scanner scanner, ICjStream writer) throws IOException {
        ICjNodeChunkMutable nodeChunk = writer.createNodeChunk();
        String id = null;
        String label = null;

        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.equals("]")) {
                if (id != null) {
                    nodeChunk.id(id);
                }
                if (label != null) {
                    nodeChunk.addLabelWithoutLanguage(label);
                }
                writer.node(nodeChunk);
                return;
            }

            String value = scanner.next();
            if (token.equalsIgnoreCase("id")) {
                id = value;
            } else if (token.equalsIgnoreCase("label")) {
                // GML labels can be quoted
                if (value.startsWith("\"")) {
                    StringBuilder quotedLabel = new StringBuilder(value.substring(1));
                    while (scanner.hasNext() && !value.endsWith("\"")) {
                        value = scanner.next();
                        quotedLabel.append(" ").append(value);
                    }
                    // unescape quotes
                    label = quotedLabel.toString().replaceAll("\"", "");
                } else {
                    label = value;
                }
            } else {
                // Capture simple attributes (non-nested) into node data; skip nested blocks
                if ("[".equals(value)) {
                    // nested block -> skip entirely
                    skipBlock(scanner, value);
                } else {
                    // value may be quoted; collect full quoted token if needed
                    String attrValue = value;
                    if (attrValue.startsWith("\"")) {
                        StringBuilder quoted = new StringBuilder(attrValue.substring(1));
                        while (scanner.hasNext() && !attrValue.endsWith("\"")) {
                            attrValue = scanner.next();
                            quoted.append(" ").append(attrValue);
                        }
                        attrValue = quoted.toString().replaceAll("\"", "");
                    }
                    final String key = token;
                    final String val = attrValue;
                    nodeChunk.dataMutable(d -> d.addProperty(key, val));
                }
            }
        }
    }

    private void processFileContent(final Scanner scanner, final ICjStream cjStream) throws IOException {
        ICjDocumentChunkMutable doc = cjStream.createDocumentChunk();
        boolean documentStarted = false;

        while (scanner.hasNext()) {
            String token = scanner.next();
            if (token.equalsIgnoreCase("graph")) {
                // On first graph, start the document (emits accumulated document-level attributes)
                if (!documentStarted) {
                    cjStream.documentStart(doc);
                    documentStarted = true;
                }
                // Expect opening bracket and parse graph
                if (scanner.hasNext() && scanner.next().equals("[")) {
                    parseGraph(scanner, cjStream);
                }
            } else if (token.equals("#")) {
                // comment till end of line; skip
                if (scanner.hasNextLine()) scanner.nextLine();
            } else if (!documentStarted) {
                // Treat as document-level attribute until the document is started
                if (!scanner.hasNext()) break;
                String value = scanner.next();
                if ("[".equals(value)) {
                    // nested block at top level - skip
                    skipBlock(scanner, value);
                } else {
                    String attrValue = value;
                    if (attrValue.startsWith("\"")) {
                        StringBuilder quoted = new StringBuilder(attrValue.substring(1));
                        while (scanner.hasNext() && !attrValue.endsWith("\"")) {
                            attrValue = scanner.next();
                            quoted.append(" ").append(attrValue);
                        }
                        attrValue = quoted.toString().replaceAll("\"", "");
                    }
                    final String key = token;
                    final String val = attrValue;
                    doc.dataMutable(d -> d.addProperty(key, val));
                }
            } else {
                // After document started: ignore stray tokens at top-level or skip nested blocks gracefully
                if (scanner.hasNext()) {
                    String maybe = scanner.next();
                    if ("[".equals(maybe)) {
                        skipBlock(scanner, maybe);
                    }
                }
            }
        }

        if (!documentStarted) {
            // no graphs encountered; still emit empty document with collected attributes
            cjStream.documentStart(doc);
        }
        cjStream.documentEnd();
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
