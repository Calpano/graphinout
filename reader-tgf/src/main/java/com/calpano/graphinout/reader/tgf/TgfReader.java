package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class TgfReader implements GioReader {
    private static final Logger log = getLogger(TgfReader.class);
    public static final String DELIMITER = " ";
    public static final String HASH = "#";
    public static final String LABEL = "label";
    /**
     * @Nullable TODO maybe make a true Optional
     */
    private Consumer<ContentError> errorConsumer;

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("tfg", "Trivial Graph Format");
    }

    public void errorHandler(Consumer<ContentError> errorConsumer) {
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        String content = IOUtils.toString(inputSource.inputStream());

        boolean isValid = false;
        Scanner scanner = new Scanner(content);
        boolean foundHash = false;
        int lineCount = 0;
        boolean nodes = true;

        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineCount++;

            if (nodes) {
                String[] nodeParts = line.split(DELIMITER);

                List<GioData> gioDataList = new ArrayList<>();
                GioData gioData = GioData.builder().key(LABEL).value(nodeParts[1]).build();
                gioDataList.add(gioData);

                writer.startNode(GioNode.builder()
                        .id(nodeParts[0])
                        .dataList(gioDataList)
                        .build());

            }
            if (line.contains(HASH)) {
                foundHash = true;
                nodes = false;
            } else {
                String[] edgeParts = line.split(DELIMITER);
                GioEndpoint sourceEndpoint = GioEndpoint.builder().id(edgeParts[0]).build();
                GioEndpoint targetEndpoint = GioEndpoint.builder().id(edgeParts[1]).build();

                List<GioEndpoint> endpointList = new ArrayList<>();
                endpointList.add(sourceEndpoint);
                endpointList.add(targetEndpoint);
                if (edgeParts.length == 3) {
                    writer.startEdge(GioEdge.builder().endpoints(endpointList)
                            .desc(edgeParts[2]).build());
                }
                writer.startEdge(GioEdge.builder().endpoints(endpointList).build());
            }
        }
        writer.endGraph();
        writer.endDocument();
        if (lineCount < 3 || !foundHash) {
            isValid = true;
        }
        scanner.close();

        if (!isValid && errorConsumer != null) {
            errorConsumer.accept(new ContentError(ContentError.ErrorLevel.Error, "TGF file is not valid.", Optional.empty()));
        }
    }
}
