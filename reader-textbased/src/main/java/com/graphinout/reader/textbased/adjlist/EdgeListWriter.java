package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Writes an {@link ICjDocument} as an Edge List: one "source target" line per edge. The inverse of
 * {@link EdgeListReader}. Isolated nodes (without any incident edge) are written as a single token on their own line
 * (the NetworkX edge-list convention), so they survive a round-trip.
 */
public class EdgeListWriter implements GioWriter {

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                outputSink.write(toEdgeList(cjDoc));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return EdgeListReader.FORMAT;
    }

    public static String toEdgeList(ICjDocument cjDoc) {
        StringBuilder sb = new StringBuilder();
        Set<String> incident = new LinkedHashSet<>();
        cjDoc.edgesAll().forEach(e -> {
            String[] st = TextEndpoints.sourceTarget(e.endpoints().toList());
            if (st != null) {
                incident.add(st[0]);
                incident.add(st[1]);
                sb.append(st[0]).append(' ').append(st[1]).append('\n');
            }
        });
        // isolated nodes (not an endpoint of any written edge): emit a lone-token line so they survive the round-trip
        cjDoc.nodesAllIncludingImplied().forEach(n -> {
            if (!incident.contains(n.id())) {
                sb.append(n.id()).append('\n');
            }
        });
        return sb.toString();
    }

}
