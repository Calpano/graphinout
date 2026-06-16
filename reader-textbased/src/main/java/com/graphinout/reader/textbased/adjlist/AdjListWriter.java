package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes an {@link ICjDocument} as an Adjacency List: one line per node, "node target1 target2 ...". The inverse of
 * {@link AdjListReader}. Every node gets its own line (so isolated nodes are preserved); a node's line lists its
 * out-edge targets.
 */
public class AdjListWriter implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                outputSink.write(toAdjList(cjDoc));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return AdjListReader.FORMAT;
    }

    public static String toAdjList(ICjDocument cjDoc) {
        // one entry per node, preserving document order; isolated nodes get an empty target list
        Map<String, List<String>> adjacency = new LinkedHashMap<>();
        cjDoc.nodesAllIncludingImplied().forEach(n -> adjacency.computeIfAbsent(n.id(), k -> new ArrayList<>()));
        cjDoc.edgesAll().forEach(e -> {
            String[] st = TextEndpoints.sourceTarget(e.endpoints().toList());
            if (st != null) {
                adjacency.computeIfAbsent(st[0], k -> new ArrayList<>()).add(st[1]);
            }
        });

        StringBuilder sb = new StringBuilder();
        adjacency.forEach((source, targets) -> {
            sb.append(source);
            for (String target : targets) {
                sb.append(' ').append(target);
            }
            sb.append('\n');
        });
        return sb.toString();
    }

}
