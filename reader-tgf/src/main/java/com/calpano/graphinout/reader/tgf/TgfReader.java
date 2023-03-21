package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

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
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class TgfReader implements GioReader {

    private @Nullable Consumer<ContentError> errorHandler;

    /**
     * Set error handler
     *
     * @param errorHandler
     */
    public void errorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }


    public static final String DELIMITER = " ";
    public static final String HASH = "#";
    public static final String LABEL = "label";
    private static final Logger log = getLogger(TgfReader.class);


    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("tfg", "Trivial Graph Format", ".tgf");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        String content = IOUtils.toString(sis.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }

        Scanner scanner = new Scanner(content);
        boolean edges = false;
        boolean nodes = false;

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());
        Set<String> nodesCreatedSet = new HashSet<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(HASH)) {
                edges = true;
                continue;
            }
            if (!edges) {
                nodes = true;
                log.info("--- nodes:");
                String[] nodeParts = line.split(DELIMITER);

                writer.startNode(GioNode.builder()
                        .id(nodeParts[0])
                        .build());
                writer.data(GioData.builder().key(LABEL).value(nodeParts[1]).build());
                writer.endNode(null);
            } else {
                log.info("--- edges:");
                String[] edgeParts = line.split(DELIMITER, 3);
                GioEndpoint sourceEndpoint = GioEndpoint.builder().node(edgeParts[0]).build();
                GioEndpoint targetEndpoint = GioEndpoint.builder().node(edgeParts[1]).build();

                List<GioEndpoint> endpointList = new ArrayList<>();
                endpointList.add(sourceEndpoint);
                endpointList.add(targetEndpoint);
                if (edgeParts.length == 2 || edgeParts.length == 3) {
                    GioEdge gioEdge = GioEdge.builder().endpoints(endpointList).build();
                    if (edgeParts.length == 3) {
                        GioData.builder().value(edgeParts[2]).build();
                    }
                    if (!nodes) {
                        for(String nodeId : Arrays.asList(edgeParts[0], edgeParts[1])) {
                            if(nodesCreatedSet.add(nodeId)) {
                                // IMPROVE create contentError warning
                                log.warn("No specified nodes found in the file for edge: " + Arrays.toString(edgeParts) + "Required nodes have been created.");
                                writer.startNode(GioNode.builder().id(nodeId).build());
                                writer.endNode(null);
                            }
                        }
                    }
                    writer.startEdge(gioEdge);
                    writer.endEdge();
                }
            }
        }
        writer.endGraph(null);
        writer.endDocument();
        scanner.close();
        if (!edges) {
            if (!nodes) {
                if(errorHandler!=null) {
                    errorHandler.accept(new ContentError(ContentError.ErrorLevel.Warn,"No nodes found in file",null));
                }
            } else {
                log.warn("No edges found in the file.");
            }
            return;
        }
    }
}