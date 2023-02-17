package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
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
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class TgfReader implements GioReader {

    private @Nullable Consumer<ContentError> errorHandler;

    /**
     * Set error handler
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
        return new GioFileFormat("tfg", "Trivial Graph Format",".tgf");
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        assert inputSource instanceof SingleInputSource;
        SingleInputSource sis = (SingleInputSource) inputSource;
        String content = IOUtils.toString(sis.inputStream(), StandardCharsets.UTF_8);

        boolean isValid = true;
        Scanner scanner = new Scanner(content);
        boolean edges = false;
        boolean nodes = false;

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

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

                List<GioData> gioDataList = new ArrayList<>();
                GioData gioData = GioData.builder().key(LABEL).value(nodeParts[1]).build();
                gioDataList.add(gioData);

                writer.startNode(GioNode.builder()
                        .id(nodeParts[0])
                        .dataList(gioDataList)
                        .build());
                writer.endNode(null);
            } else {
                log.info("--- edges:");
                String[] edgeParts = line.split(DELIMITER);
                GioEndpoint sourceEndpoint = GioEndpoint.builder().node(edgeParts[0]).build();
                GioEndpoint targetEndpoint = GioEndpoint.builder().node(edgeParts[1]).build();

                List<GioEndpoint> endpointList = new ArrayList<>();
                endpointList.add(sourceEndpoint);
                endpointList.add(targetEndpoint);
                if (edgeParts.length == 3) {
                    writer.startEdge(GioEdge.builder().endpoints(endpointList)
                            .description(edgeParts[2]).build());
                }
                writer.startEdge(GioEdge.builder().endpoints(endpointList).build());
                writer.endEdge();
            }
        }
        writer.endGraph(null);
        writer.endDocument();
        if (edges && !nodes) {
            isValid = false;
        }
        scanner.close();

        if (!isValid && errorHandler != null) {
            errorHandler.accept(new ContentError(ContentError.ErrorLevel.Error, "TGF file is not valid.", null));
        }
    }
}