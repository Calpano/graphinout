package com.calpano.graphinout.reader.adjlist;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;

public class AdjListReader implements GioReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdjListReader.class);
    private static final String DELIMITER = " ";
    private static final String HASH = "#";
    private static final String LABEL = "label";
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("adjlist", "Adjacency List Format", ".adjlist");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }

        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }
        try (Scanner scanner = new Scanner(content)) {
            processFileContent(scanner, writer);
        }
    }

    private void processFileContent(final Scanner scanner, final GioWriter writer) throws IOException {
        boolean edges = false;

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        Set<String> nodesCreatedSet = new HashSet<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            processLine(line, writer, nodesCreatedSet);
        }

        writer.endGraph(null);
        writer.endDocument();
    }

    private void processLine(final String inputLine, final GioWriter writer, final Set<String> nodesCreatedSet) throws IOException {
        // trim comment
        String line = inputLine;
        int commentIndex = line.indexOf(HASH);
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }

        String[] parts = line.split(DELIMITER);
        String sourceId = null;
        GioEndpoint sourceEndpoint = null;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            // create nodes for all parts that have not yet been created
            if (nodesCreatedSet.add(part)) {
                writer.startNode(GioNode.builder().id(part).build());
            //    writer.data(GioData.builder().key(LABEL).value(part).build());
                writer.endNode(null);
            }
            if (i == 0) {
                sourceId = part;
                sourceEndpoint = GioEndpoint.builder().node(sourceId).build();
            } else {
                String targetId = parts[i];
                GioEndpoint targetEndpoint = GioEndpoint.builder().node(targetId).build();
                List<GioEndpoint> endpointList = Arrays.asList(sourceEndpoint, targetEndpoint);
                GioEdge gioEdge = GioEdge.builder().endpoints(endpointList).build();
                writer.startEdge(gioEdge);
                writer.endEdge();
            }
        }
    }

}

