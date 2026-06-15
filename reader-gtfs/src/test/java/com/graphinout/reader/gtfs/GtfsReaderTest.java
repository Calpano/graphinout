package com.graphinout.reader.gtfs;

import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.factory.CjFactory;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.input.MultiInputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.value.java.JavaJsonFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GtfsReaderTest {

    private static final List<String> MINI_FEED_FILES = List.of( //
            "agency.txt", "levels.txt", "routes.txt", "stops.txt", "transfers.txt", "pathways.txt", //
            "trips.txt", "stop_times.txt");

    // mini feed: 3 stations + 11 platforms/entrances/nodes; 3 transfers; 3 pathways (2 bidirectional);
    // ostkreuz has 2 platforms but no transfers/pathways -> 1 synthesized edge; routes U1 (2 trips), S1 (1 trip)
    private static final int EXPECTED_NODES = 14;
    private static final int EXPECTED_TRANSFER_EDGES = 3;
    private static final int EXPECTED_PATHWAY_EDGES = 5; // 2x stairs, 2x elevator, 1x exit-gate
    private static final int EXPECTED_SYNTHESIZED_EDGES = 1;
    private static final int EXPECTED_U1_EDGES = 4; // 2 per direction
    private static final int EXPECTED_S1_EDGES = 2; // one direction only
    private static final int EXPECTED_TRAVEL_EDGES = EXPECTED_U1_EDGES + EXPECTED_S1_EDGES;
    private static final int EXPECTED_EDGES = EXPECTED_TRANSFER_EDGES + EXPECTED_PATHWAY_EDGES //
            + EXPECTED_SYNTHESIZED_EDGES + EXPECTED_TRAVEL_EDGES;
    private static final int EXPECTED_GRAPHS = 3; // base + route:s1 + route:u1

    private AutoCloseable closeable;
    private GtfsReader underTest;
    private List<ContentError> errors;
    @Mock private ICjStream mockCjStream;

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        underTest = new GtfsReader();
        errors = new ArrayList<>();
        underTest.setContentErrorHandler(errors::add);

        CjFactory factory = new CjFactory();
        when(mockCjStream.createDocumentChunk()).thenAnswer(inv -> factory.createDocumentChunk());
        when(mockCjStream.createGraphChunk()).thenAnswer(inv -> factory.createGraphChunk());
        when(mockCjStream.createNodeChunk()).thenAnswer(inv -> factory.createNodeChunk());
        when(mockCjStream.createEdgeChunk()).thenAnswer(inv -> factory.createEdgeChunk());
        when(mockCjStream.jsonFactory()).thenReturn(JavaJsonFactory.INSTANCE);
    }

    @Test
    void shouldEmitTwoLevelModel() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        verify(mockCjStream, times(1)).documentStart(any(ICjDocumentChunk.class));
        verify(mockCjStream, times(EXPECTED_GRAPHS)).graphStart(any(ICjGraphChunk.class));
        verify(mockCjStream, times(EXPECTED_NODES)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(EXPECTED_EDGES)).edge(any(ICjEdgeChunk.class));
        verify(mockCjStream, times(EXPECTED_GRAPHS)).graphEnd();
        verify(mockCjStream, times(1)).documentEnd();
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldEmitBaseGraphFirstThenRouteSubgraphsSortedByName() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        ArgumentCaptor<ICjGraphChunk> graphs = ArgumentCaptor.forClass(ICjGraphChunk.class);
        verify(mockCjStream, times(EXPECTED_GRAPHS)).graphStart(graphs.capture());
        List<String> graphIds = graphs.getAllValues().stream().map(ICjGraphChunk::id).toList();
        assertThat(graphIds).containsExactly("base", "route:s1", "route:u1").inOrder();
    }

    @Test
    void shouldEmitStationsAndPlatformsWithoutRollup() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        ArgumentCaptor<ICjNodeChunk> nodes = ArgumentCaptor.forClass(ICjNodeChunk.class);
        verify(mockCjStream, times(EXPECTED_NODES)).node(nodes.capture());
        Set<String> nodeIds = nodes.getAllValues().stream().map(ICjNodeChunk::id).collect(Collectors.toSet());
        // stations AND their platforms/entrances/in-station nodes are separate nodes of the base graph
        assertThat(nodeIds).containsExactly( //
                "warschauer", "warschauer-u1", "schlesisches-tor", "goerlitzer-bf", //
                "friedrichstr", "friedrichstr-s", "friedrichstr-u6", "friedrichstr-halle", "friedrichstr-eingang", //
                "brandenburger-tor", "potsdamer-platz", "ostkreuz", "ostkreuz-ring", "ostkreuz-stadtbahn");

        ICjNodeChunk platform = nodeById(nodes.getAllValues(), "warschauer-u1");
        String dataJson = Objects.requireNonNull(platform.data().jsonValue()).toJsonString();
        assertThat(dataJson).contains("\"parentStation\":\"warschauer\"");
        assertThat(dataJson).contains("\"platformCode\":\"1\"");
        assertThat(dataJson).contains("\"locationType\":\"0\"");

        ICjNodeChunk concourse = nodeById(nodes.getAllValues(), "friedrichstr-halle");
        String concourseJson = Objects.requireNonNull(concourse.data().jsonValue()).toJsonString();
        assertThat(concourseJson).contains("\"locationType\":\"3\"");
        assertThat(concourseJson).contains("\"levelName\":\"Straßenniveau\"");
    }

    @Test
    void shouldMapWheelchairBoardingWithParentInheritance() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        ArgumentCaptor<ICjNodeChunk> nodes = ArgumentCaptor.forClass(ICjNodeChunk.class);
        verify(mockCjStream, times(EXPECTED_NODES)).node(nodes.capture());

        // station declares wheelchair_boarding=1
        assertThat(nodeDataJson(nodes, "warschauer")).contains("\"isWheelchairAccessible\":true");
        // child platform with empty value inherits from its parent station
        assertThat(nodeDataJson(nodes, "warschauer-u1")).contains("\"isWheelchairAccessible\":true");
        // child platform with an own value (2) overrides the parent's 1
        assertThat(nodeDataJson(nodes, "friedrichstr-u6")).contains("\"isWheelchairAccessible\":false");
        // no information anywhere: property is omitted, not defaulted
        assertThat(nodeDataJson(nodes, "schlesisches-tor")).doesNotContain("isWheelchairAccessible");
    }

    @Test
    void shouldCountWheelchairAccessibleTripsOnTravelEdges() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        List<ICjEdgeChunk> travelEdges = capturedEdges().subList(EXPECTED_EDGES - EXPECTED_TRAVEL_EDGES, EXPECTED_EDGES);
        // u1-east is declared wheelchair-accessible (1)
        ICjEdgeChunk u1East = Objects.requireNonNull( //
                findDirectedEdge(travelEdges, "goerlitzer-bf", "schlesisches-tor"));
        assertThat(Objects.requireNonNull(u1East.data().jsonValue()).toJsonString()) //
                .contains("\"wheelchairAccessibleTripCount\":1");
        // u1-west has no information, s1-south is explicitly not accessible (2): property omitted
        ICjEdgeChunk u1West = Objects.requireNonNull( //
                findDirectedEdge(travelEdges, "schlesisches-tor", "goerlitzer-bf"));
        assertThat(Objects.requireNonNull(u1West.data().jsonValue()).toJsonString()) //
                .doesNotContain("wheelchairAccessibleTripCount");
        ICjEdgeChunk s1 = Objects.requireNonNull( //
                findDirectedEdge(travelEdges, "friedrichstr-s", "brandenburger-tor"));
        assertThat(Objects.requireNonNull(s1.data().jsonValue()).toJsonString()) //
                .doesNotContain("wheelchairAccessibleTripCount");
    }

    private static String nodeDataJson(ArgumentCaptor<ICjNodeChunk> nodes, String nodeId) {
        return Objects.requireNonNull(nodeById(nodes.getAllValues(), nodeId).data().jsonValue()).toJsonString();
    }

    @Test
    void shouldEmitTransfersWithRouteConditions() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        List<ICjEdgeChunk> edges = capturedEdges();
        // emission order: transfer edges first
        List<ICjEdgeChunk> transferEdges = edges.subList(0, EXPECTED_TRANSFER_EDGES);
        for (ICjEdgeChunk edge : transferEdges) {
            assertThat(Objects.requireNonNull(edge.edgeType()).type()).isEqualTo("transfer");
        }
        // travel edges (within route subgraphs) have no edge type
        for (ICjEdgeChunk edge : edges.subList(EXPECTED_EDGES - EXPECTED_TRAVEL_EDGES, edges.size())) {
            assertThat(edge.edgeType()).isNull();
        }

        ICjEdgeChunk footpath = transferEdges.stream() //
                .filter(e -> "friedrichstr-s".equals(Objects.requireNonNull(e.source()).node())) //
                .findFirst().orElseThrow();
        assertThat(Objects.requireNonNull(footpath.target()).node()).isEqualTo("friedrichstr-u6");
        String dataJson = Objects.requireNonNull(footpath.data().jsonValue()).toJsonString();
        assertThat(dataJson).contains("\"minTransferTime\":180");
        assertThat(dataJson).contains("\"transferType\":\"2\"");
        // route-conditional transfer (GTFS transfers v2): condition is stored as data, never as an edge
        // between route subgraphs
        assertThat(dataJson).contains("\"fromRouteId\":\"s1\"");
        assertThat(dataJson).contains("\"toRouteId\":\"u1\"");
    }

    @Test
    void shouldEmitPathwayNetwork() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        List<ICjEdgeChunk> edges = capturedEdges();
        List<ICjEdgeChunk> pathwayEdges = edges.subList(EXPECTED_TRANSFER_EDGES, //
                EXPECTED_TRANSFER_EDGES + EXPECTED_PATHWAY_EDGES);
        List<String> types = pathwayEdges.stream() //
                .map(e -> Objects.requireNonNull(e.edgeType()).type()).toList();
        // bidirectional pathways become two directed edges (stairs cost differently up vs down)
        assertThat(types).containsExactly("stairs", "stairs", "elevator", "elevator", "exit-gate").inOrder();

        ICjEdgeChunk stairsDown = pathwayEdges.get(0);
        assertThat(Objects.requireNonNull(stairsDown.source()).node()).isEqualTo("friedrichstr-s");
        assertThat(Objects.requireNonNull(stairsDown.target()).node()).isEqualTo("friedrichstr-halle");
        String stairsJson = Objects.requireNonNull(stairsDown.data().jsonValue()).toJsonString();
        assertThat(stairsJson).contains("\"pathwayId\":\"pw-stairs\"");
        assertThat(stairsJson).contains("\"traversalTime\":45");
        assertThat(stairsJson).contains("\"stairCount\":24");
        assertThat(stairsJson).contains("\"signpostedAs\":\"Halle\"");
        assertThat(stairsJson).contains("\"isWheelchairAccessible\":false");

        ICjEdgeChunk stairsUp = pathwayEdges.get(1);
        assertThat(Objects.requireNonNull(stairsUp.source()).node()).isEqualTo("friedrichstr-halle");
        String stairsUpJson = Objects.requireNonNull(stairsUp.data().jsonValue()).toJsonString();
        assertThat(stairsUpJson).contains("\"signpostedAs\":\"Gleis S-Bahn\"");

        ICjEdgeChunk lift = pathwayEdges.get(2);
        String liftJson = Objects.requireNonNull(lift.data().jsonValue()).toJsonString();
        assertThat(liftJson).contains("\"isWheelchairAccessible\":true");
        assertThat(liftJson).contains("\"minWidth\":1.1");
    }

    @Test
    void shouldSynthesizeFallbackTransferForUncoveredStations() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        List<ICjEdgeChunk> edges = capturedEdges();
        List<ICjEdgeChunk> synthesized = edges.stream() //
                .filter(e -> e.data().jsonValue() != null //
                        && e.data().jsonValue().toJsonString().contains("\"synthesized\":true")) //
                .toList();
        // only ostkreuz (2 platforms, no transfers/pathways) gets a fallback edge;
        // friedrichstr is covered by measured data, warschauer has just one platform
        assertThat(synthesized).hasSize(EXPECTED_SYNTHESIZED_EDGES);
        ICjEdgeChunk edge = synthesized.getFirst();
        assertThat(Objects.requireNonNull(edge.edgeType()).type()).isEqualTo("transfer");
        assertThat(edge.undirectedEndpoints()).hasSize(2);
        Set<String> endpointNodes = edge.endpoints().map(ep -> ep.node()).collect(Collectors.toSet());
        assertThat(endpointNodes).containsExactly("ostkreuz-ring", "ostkreuz-stadtbahn");
    }

    @Test
    void shouldEmitDirectedTravelEdgesPerDirection() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        List<ICjEdgeChunk> edges = capturedEdges();
        List<ICjEdgeChunk> travelEdges = edges.subList(EXPECTED_TRANSFER_EDGES, edges.size());

        // out and return direction of U1 stay separate directed edges
        assertThat(findDirectedEdge(travelEdges, "goerlitzer-bf", "schlesisches-tor")).isNotNull();
        assertThat(findDirectedEdge(travelEdges, "schlesisches-tor", "goerlitzer-bf")).isNotNull();
        // U1 serves its own platform of the Warschauer Str. station, not the station node
        assertThat(findDirectedEdge(travelEdges, "schlesisches-tor", "warschauer-u1")).isNotNull();
        // S1 ran only one direction in this feed
        assertThat(findDirectedEdge(travelEdges, "friedrichstr-s", "brandenburger-tor")).isNotNull();
        assertThat(findDirectedEdge(travelEdges, "brandenburger-tor", "friedrichstr-s")).isNull();
    }

    @Test
    void shouldAggregateTripMetadataOnTravelEdges() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        List<ICjEdgeChunk> travelEdges = capturedEdges().subList(EXPECTED_TRANSFER_EDGES, EXPECTED_EDGES);
        ICjEdgeChunk u1East = Objects.requireNonNull( //
                findDirectedEdge(travelEdges, "goerlitzer-bf", "schlesisches-tor"));
        String dataJson = Objects.requireNonNull(u1East.data().jsonValue()).toJsonString();
        assertThat(dataJson).contains("\"tripCount\":1");
        assertThat(dataJson).contains("\"shapeIds\":[\"shp-u1-east\"]");
        assertThat(dataJson).contains("\"directionIds\":[\"0\"]");
    }

    @Test
    void shouldPreserveCommaInQuotedStopName() throws IOException {
        underTest.read(miniFeedAsZip(), mockCjStream);

        ArgumentCaptor<ICjNodeChunk> nodes = ArgumentCaptor.forClass(ICjNodeChunk.class);
        verify(mockCjStream, times(EXPECTED_NODES)).node(nodes.capture());
        ICjNodeChunk friedrichstr = nodeById(nodes.getAllValues(), "friedrichstr");
        assertThat(Objects.requireNonNull(Objects.requireNonNull(friedrichstr.label()).theEntry()).value()) //
                .isEqualTo("Friedrichstr., Bahnhof");
    }

    @Test
    void shouldReadMiniFeedFromMultiInputSource() throws IOException {
        underTest.read(miniFeedAsMultiSource(), mockCjStream);

        verify(mockCjStream, times(EXPECTED_NODES)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(EXPECTED_EDGES)).edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldWorkWithoutOptionalTransfersFile() throws IOException {
        Map<String, String> files = new HashMap<>();
        for (String fileName : MINI_FEED_FILES) {
            if (!fileName.equals("transfers.txt")) files.put(fileName, miniFeedFileContent(fileName));
        }
        underTest.read(zipOf(files), mockCjStream);

        verify(mockCjStream, times(EXPECTED_NODES)).node(any(ICjNodeChunk.class));
        verify(mockCjStream, times(EXPECTED_PATHWAY_EDGES + EXPECTED_SYNTHESIZED_EDGES + EXPECTED_TRAVEL_EDGES)) //
                .edge(any(ICjEdgeChunk.class));
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReportErrorOnMissingRequiredFile() throws IOException {
        Map<String, String> files = new HashMap<>();
        for (String fileName : MINI_FEED_FILES) {
            if (!fileName.equals("stops.txt")) files.put(fileName, miniFeedFileContent(fileName));
        }
        underTest.read(zipOf(files), mockCjStream);

        assertThat(errors.stream().filter(ContentError::isError).count()).isEqualTo(1L);
        assertThat(errors.getFirst().getMessage()).contains("stops.txt");
        // still emits a valid (empty) document
        verify(mockCjStream).document(any(ICjDocumentChunk.class));
    }

    @Test
    void shouldFailValidationOnGarbageInput() throws IOException {
        assertThat(underTest.isValid(SingleInputSource.of("garbage.gtfs.zip", "this is not a zip file"))).isFalse();
    }

    /** Also proves that route-subgraph edges referencing base-graph nodes pass {@code ValidatingCjWriter}. */
    @Test
    void shouldPassValidationOnMiniFeed() throws IOException {
        assertThat(underTest.isValid(miniFeedAsZip())).isTrue();
    }

    // ---------------------------------------------------------------- helpers

    private List<ICjEdgeChunk> capturedEdges() {
        ArgumentCaptor<ICjEdgeChunk> edges = ArgumentCaptor.forClass(ICjEdgeChunk.class);
        verify(mockCjStream, times(EXPECTED_EDGES)).edge(edges.capture());
        return edges.getAllValues();
    }

    private static ICjNodeChunk nodeById(List<ICjNodeChunk> nodes, String id) {
        return nodes.stream().filter(n -> id.equals(n.id())).findFirst().orElseThrow();
    }

    private static @org.jspecify.annotations.Nullable ICjEdgeChunk findDirectedEdge( //
            List<ICjEdgeChunk> edges, String sourceNode, String targetNode) {
        return edges.stream() //
                .filter(e -> e.source() != null && e.target() != null) //
                .filter(e -> sourceNode.equals(Objects.requireNonNull(e.source()).node()) //
                        && targetNode.equals(Objects.requireNonNull(e.target()).node())) //
                .findFirst().orElse(null);
    }

    private static String miniFeedFileContent(String fileName) {
        String resourcePath = "/text/gtfs/mini/" + fileName;
        try (InputStream in = GtfsReaderTest.class.getResourceAsStream(resourcePath)) {
            Objects.requireNonNull(in, "Missing test resource " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static SingleInputSource miniFeedAsZip() {
        Map<String, String> files = new HashMap<>();
        for (String fileName : MINI_FEED_FILES) {
            files.put(fileName, miniFeedFileContent(fileName));
        }
        return zipOf(files);
    }

    private static SingleInputSource zipOf(Map<String, String> files) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
            }
            return SingleInputSource.of(bytes.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static MultiInputSource miniFeedAsMultiSource() {
        Map<String, SingleInputSource> sources = new HashMap<>();
        for (String fileName : MINI_FEED_FILES) {
            sources.put(fileName, SingleInputSource.of(fileName, miniFeedFileContent(fileName)));
        }
        return new MultiInputSource() {
            @Override
            public SingleInputSource getNamedSource(String name) {
                return sources.get(name);
            }

            @Override
            public String name() {
                return "mini-gtfs-feed";
            }

            @Override
            public Set<String> names() {
                return sources.keySet();
            }

            @Override
            public void close() {
            }
        };
    }
}
