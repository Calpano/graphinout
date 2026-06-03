package com.graphinout.reader.mermaid;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.mermaid.parsers.ArchitectureParser;
import com.graphinout.reader.mermaid.parsers.C4Parser;
import com.graphinout.reader.mermaid.parsers.ClassDiagramParser;
import com.graphinout.reader.mermaid.parsers.ErDiagramParser;
import com.graphinout.reader.mermaid.parsers.FlowchartParser;
import com.graphinout.reader.mermaid.parsers.MermaidParseCtx;
import com.graphinout.reader.mermaid.parsers.MindmapParser;
import com.graphinout.reader.mermaid.parsers.SankeyParser;
import com.graphinout.reader.mermaid.parsers.StateDiagramParser;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reads Mermaid diagrams and emits CJ events. The first non-blank, non-comment, non-config-block line
 * determines the diagram type; subsequent lines are dispatched to the matching {@code *Parser}.
 */
public class MermaidReader implements GioReader {

    public static final String FORMAT_ID = "mermaid";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Mermaid", ".mmd", ".mermaid");

    private @Nullable Consumer<ContentError> errorHandler;

    public static ICjDocument parseMermaidToCjDocument(SingleInputSource inputSource) throws IOException {
        MermaidReader reader = new MermaidReader();
        CjWriter2CjDocumentWriter cj2document = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2cj = new CjStream2CjWriter(cj2document, true);
        reader.read(inputSource, cjStream2cj);
        return cj2document.resultDoc();
    }

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream writer) throws IOException {
        if (inputSource.isMulti()) throw new IllegalArgumentException("Cannot handle multi-sources");
        SingleInputSource sis = (SingleInputSource) inputSource;
        String content = IOUtils.toString(sis.inputStream(), StandardCharsets.UTF_8);

        if (content.isBlank()) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(ContentError.ErrorLevel.Warn, "Content is empty"));
            writer.document(writer.createDocumentChunk());
            return;
        }

        List<String> rawLines = content.lines().toList();
        Stripped stripped = stripCommentsAndConfig(rawLines);
        if (stripped.headerIndex < 0) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(ContentError.ErrorLevel.Warn, "No diagram header found"));
            writer.document(writer.createDocumentChunk());
            return;
        }
        String headerLine = stripped.lines.get(stripped.headerIndex);
        @Nullable MermaidDiagramType type = MermaidDiagramType.detect(headerLine);
        if (type == null) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(
                    ContentError.ErrorLevel.Warn, "Unknown Mermaid diagram type: " + headerLine));
            writer.document(writer.createDocumentChunk());
            return;
        }

        writer.documentStart(writer.createDocumentChunk());
        writer.graphStart(writer.createGraphChunk());

        MermaidParseCtx ctx = new MermaidParseCtx(writer, errorHandler);
        List<String> body = stripped.lines.subList(stripped.headerIndex + 1, stripped.lines.size());
        int headerLineNumber = stripped.headerIndex + 1; // 1-based

        switch (type) {
            case FLOWCHART -> new FlowchartParser().parse(headerLine, body, ctx, headerLineNumber);
            case CLASS_DIAGRAM -> new ClassDiagramParser().parse(headerLine, body, ctx, headerLineNumber);
            case STATE_DIAGRAM -> new StateDiagramParser().parse(headerLine, body, ctx, headerLineNumber);
            case ER_DIAGRAM -> new ErDiagramParser().parse(headerLine, body, ctx, headerLineNumber);
            case C4 -> new C4Parser().parse(headerLine, body, ctx, headerLineNumber);
            case MINDMAP -> new MindmapParser().parse(headerLine, body, ctx, headerLineNumber);
            case SANKEY -> new SankeyParser().parse(headerLine, body, ctx, headerLineNumber);
            case ARCHITECTURE -> new ArchitectureParser().parse(headerLine, body, ctx, headerLineNumber);
        }

        if (!ctx.hasContent()) {
            Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(
                    ContentError.ErrorLevel.Warn, "Diagram has no nodes or edges"));
        }
        ctx.flush();

        writer.graphEnd();
        writer.documentEnd();
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    static final class Stripped {
        final List<String> lines;
        final int headerIndex;

        Stripped(List<String> lines, int headerIndex) {
            this.lines = lines;
            this.headerIndex = headerIndex;
        }
    }

    /**
     * Strip {@code %%} line comments and {@code %%{init: ...}%%} configuration blocks. Returns the resulting
     * lines (same indexing — comments become empty strings) together with the index of the first content
     * line, which is the diagram header.
     */
    static Stripped stripCommentsAndConfig(List<String> raw) {
        List<String> out = new ArrayList<>(raw.size());
        int headerIndex = -1;
        for (int i = 0; i < raw.size(); i++) {
            String line = raw.get(i);
            String t = line.trim();
            if (t.isEmpty()) {
                out.add("");
                continue;
            }
            if (t.startsWith("%%{") && t.endsWith("}%%")) {
                // Single-line config block
                out.add("");
                continue;
            }
            if (t.startsWith("%%")) {
                // Line comment
                out.add("");
                continue;
            }
            out.add(line);
            if (headerIndex < 0) headerIndex = i;
        }
        return new Stripped(out, headerIndex);
    }
}
