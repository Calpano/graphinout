package com.graphinout.reader.gtfs;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocumentChunkMutable;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.foundation.pure.functional.Nullables;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Converts a GTFS feed into a <b>two-level CJ model</b>:
 *
 * <h2>Level 1: the base graph (physical world)</h2>
 * The first graph in the document is the bare infrastructure:
 * <ul>
 *     <li><b>Nodes</b> = stations ({@code location_type=1}), platforms/tracks ({@code 0}), entrances ({@code 2}),
 *     generic in-station nodes ({@code 3}) and boarding areas ({@code 4}). Containment stays flat: nodes carry a
 *     {@code parentStation} data property; no rollup happens.</li>
 *     <li><b>Edges</b> = exclusively physical connections, in three tiers per station (best available wins):
 *     <ol>
 *         <li><b>Pathways</b> from {@code pathways.txt}: edge type per {@code pathway_mode} (walkway, stairs,
 *         escalator, elevator, fare-gate, ...), bidirectional rows become two directed edges (stairs cost
 *         differently up vs down), raw constraints ({@code traversalTime}, {@code stairCount}, {@code minWidth},
 *         ...) plus a derived {@code isWheelchairAccessible} flag as data.</li>
 *         <li><b>Transfers</b> from {@code transfers.txt}: edge type {@code transfer}, with {@code transferType},
 *         {@code minTransferTime} and the route/trip-conditional v2 fields ({@code fromRouteId}, ...) as data.</li>
 *         <li><b>Synthesized fallback</b>: a station with two or more platforms but no intra-station pathway or
 *         transfer gets undirected platform-platform edges flagged {@code synthesized=true}, so routers can
 *         distinguish guessed from measured connectivity.</li>
 *     </ol></li>
 * </ul>
 * No vehicle moves on this level.
 *
 * <h2>Level 2: one subgraph per route (logical world)</h2>
 * Each route (e.g. "U1") becomes its own graph (id {@code route:<route_id>}), following the document's base graph.
 * <ul>
 *     <li><b>Nodes</b>: none are declared - the route's edges <em>reference</em> the platform nodes of level 1 by id
 *     (CJ node ids are document-scoped).</li>
 *     <li><b>Edges</b> = directed travel edges between consecutive platforms, derived from {@code stop_times.txt}.
 *     Branchings (Y-shaped routes) are simply nodes with several outgoing edges; outward and return direction stay
 *     separate. Edge data aggregates all trips over the segment: {@code tripCount}, distinct {@code shapeIds},
 *     distinct {@code directionIds}.</li>
 * </ul>
 * A transfer between two routes is <em>never</em> an edge between two route subgraphs - it always happens on level 1
 * between platforms.
 * <p>
 * Timetable details (times, calendars, frequencies) are intentionally out of scope - they are not graph topology.
 */
class Gtfs2Cj {

    static final String FILE_AGENCY = "agency.txt";
    static final String FILE_LEVELS = "levels.txt";
    static final String FILE_PATHWAYS = "pathways.txt";
    static final String FILE_ROUTES = "routes.txt";
    static final String FILE_STOPS = "stops.txt";
    static final String FILE_TRANSFERS = "transfers.txt";
    static final String FILE_TRIPS = "trips.txt";
    static final String FILE_STOP_TIMES = "stop_times.txt";

    static final String BASE_GRAPH_ID = "base";
    static final String ROUTE_GRAPH_ID_PREFIX = "route:";
    static final String EDGE_TYPE_TRANSFER = "transfer";

    /** GTFS pathway_mode → CJ edge type */
    private static final Map<String, String> PATHWAY_MODES = Map.of( //
            "1", "walkway", "2", "stairs", "3", "moving-sidewalk", "4", "escalator", //
            "5", "elevator", "6", "fare-gate", "7", "exit-gate");
    private static final Set<String> PATHWAY_MODES_NOT_WHEELCHAIR_ACCESSIBLE = Set.of("2", "4"); // stairs, escalator

    private record Stop(String name, @Nullable Double lat, @Nullable Double lon, String parentId,
                        String locationType, String platformCode, String levelId, String wheelchairBoarding) {

        /** stations, platforms, entrances, generic nodes, boarding areas */
        boolean isEmittableLocation() {
            return locationType.isEmpty() || List.of("0", "1", "2", "3", "4").contains(locationType);
        }

        boolean isPlatform() {
            return locationType.isEmpty() || locationType.equals("0");
        }

        /** the station complex this stop belongs to: its parent station, or itself */
        String complexId(String stopId) {
            return parentId.isEmpty() ? stopId : parentId;
        }
    }

    private record Route(String shortName, String longName, String type, String color, String agencyId) {

        String displayName(String routeId) {
            return !shortName.isEmpty() ? shortName : !longName.isEmpty() ? longName : routeId;
        }
    }

    private record Trip(String routeId, String directionId, String shapeId, String wheelchairAccessible) {}

    private record Transfer(String fromStopId, String toStopId, String transferType, String minTransferTime,
                            String fromRouteId, String toRouteId, String fromTripId, String toTripId) {}

    private record Pathway(String id, String fromStopId, String toStopId, String mode, boolean bidirectional,
                           @Nullable Double length, @Nullable Double traversalTime, @Nullable Double stairCount,
                           @Nullable Double maxSlope, @Nullable Double minWidth, String signpostedAs,
                           String reversedSignpostedAs) {}

    private record Level(@Nullable Double index, String name) {}

    /** A directed travel edge within one route subgraph, aggregated over all trips traversing it. */
    private static final class TravelEdge {
        int tripCount;
        int wheelchairAccessibleTripCount;
        final Set<String> shapeIds = new TreeSet<>();
        final Set<String> directionIds = new TreeSet<>();
    }

    private final GtfsFiles files;
    private final @Nullable Consumer<ContentError> errorHandler;

    private final Map<String, Stop> stops = new HashMap<>();
    private final Map<String, Route> routes = new HashMap<>();
    private final Map<String, Trip> trips = new HashMap<>();
    private final Map<String, String> agencyNames = new HashMap<>();
    private final List<Transfer> transfers = new ArrayList<>();
    private final List<Pathway> pathways = new ArrayList<>();
    private final Map<String, Level> levels = new HashMap<>();
    /** station complexes that have at least one intra-complex transfer or pathway edge */
    private final Set<String> coveredComplexes = new HashSet<>();

    /** routeId → ("fromStopId\ntoStopId" → aggregated travel edge) */
    private final Map<String, Map<String, TravelEdge>> routeEdges = new HashMap<>();

    private int unknownStopRefs = 0;
    private int unknownTripRefs = 0;
    private int badSequenceValues = 0;
    private int nonContiguousTrips = 0;
    private int unknownTransferStopRefs = 0;
    private int unknownPathwayStopRefs = 0;

    Gtfs2Cj(GtfsFiles files, @Nullable Consumer<ContentError> errorHandler) {
        this.files = files;
        this.errorHandler = errorHandler;
    }

    void read(ICjStream cj) throws IOException {
        if (!parseFeed()) {
            // required file missing: error already reported, emit a valid empty document
            cj.document(cj.createDocumentChunk());
            return;
        }
        reportSummaryWarnings();
        emit(cj);
    }

    // ---------------------------------------------------------------- parsing

    /** @return false iff a required file is missing */
    private boolean parseFeed() throws IOException {
        try (Reader r = files.open(FILE_AGENCY)) {
            if (r != null) parseAgency(r);
        }
        Reader routesReader = files.open(FILE_ROUTES);
        if (routesReader == null) return missingRequiredFile(FILE_ROUTES);
        try (Reader r = routesReader) {
            parseRoutes(r);
        }
        Reader stopsReader = files.open(FILE_STOPS);
        if (stopsReader == null) return missingRequiredFile(FILE_STOPS);
        try (Reader r = stopsReader) {
            parseStops(r);
        }
        try (Reader r = files.open(FILE_LEVELS)) {
            if (r != null) parseLevels(r);
        }
        try (Reader r = files.open(FILE_TRANSFERS)) {
            if (r != null) parseTransfers(r);
        }
        try (Reader r = files.open(FILE_PATHWAYS)) {
            if (r != null) parsePathways(r);
        }
        Reader tripsReader = files.open(FILE_TRIPS);
        if (tripsReader == null) return missingRequiredFile(FILE_TRIPS);
        try (Reader r = tripsReader) {
            parseTrips(r);
        }
        Reader stopTimesReader = files.open(FILE_STOP_TIMES);
        if (stopTimesReader == null) return missingRequiredFile(FILE_STOP_TIMES);
        try (Reader r = stopTimesReader) {
            parseStopTimes(r);
        }
        return true;
    }

    private void parseAgency(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String name = row.getOrDefault("agency_name", "");
            if (name.isEmpty()) continue;
            agencyNames.put(row.getOrDefault("agency_id", ""), name);
        }
    }

    private void parseRoutes(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String id = row.getOrDefault("route_id", "");
            if (id.isEmpty()) continue;
            routes.put(id, new Route( //
                    row.getOrDefault("route_short_name", ""), //
                    row.getOrDefault("route_long_name", ""), //
                    row.getOrDefault("route_type", ""), //
                    row.getOrDefault("route_color", ""), //
                    row.getOrDefault("agency_id", "")));
        }
    }

    private void parseStops(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String id = row.getOrDefault("stop_id", "");
            if (id.isEmpty()) continue;
            stops.put(id, new Stop( //
                    row.getOrDefault("stop_name", id), //
                    parseDoubleOrNull(row.get("stop_lat")), //
                    parseDoubleOrNull(row.get("stop_lon")), //
                    row.getOrDefault("parent_station", ""), //
                    row.getOrDefault("location_type", ""), //
                    row.getOrDefault("platform_code", ""), //
                    row.getOrDefault("level_id", ""), //
                    row.getOrDefault("wheelchair_boarding", "")));
        }
    }

    private void parseTransfers(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String from = row.getOrDefault("from_stop_id", "");
            String to = row.getOrDefault("to_stop_id", "");
            if (from.isEmpty() || to.isEmpty()) continue;
            transfers.add(new Transfer(from, to, //
                    row.getOrDefault("transfer_type", ""), row.getOrDefault("min_transfer_time", ""), //
                    row.getOrDefault("from_route_id", ""), row.getOrDefault("to_route_id", ""), //
                    row.getOrDefault("from_trip_id", ""), row.getOrDefault("to_trip_id", "")));
        }
    }

    private void parsePathways(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String from = row.getOrDefault("from_stop_id", "");
            String to = row.getOrDefault("to_stop_id", "");
            if (from.isEmpty() || to.isEmpty()) continue;
            pathways.add(new Pathway( //
                    row.getOrDefault("pathway_id", ""), from, to, //
                    row.getOrDefault("pathway_mode", ""), //
                    "1".equals(row.getOrDefault("is_bidirectional", "")), //
                    parseDoubleOrNull(row.get("length")), //
                    parseDoubleOrNull(row.get("traversal_time")), //
                    parseDoubleOrNull(row.get("stair_count")), //
                    parseDoubleOrNull(row.get("max_slope")), //
                    parseDoubleOrNull(row.get("min_width")), //
                    row.getOrDefault("signposted_as", ""), //
                    row.getOrDefault("reversed_signposted_as", "")));
        }
    }

    private void parseLevels(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String id = row.getOrDefault("level_id", "");
            if (id.isEmpty()) continue;
            levels.put(id, new Level(parseDoubleOrNull(row.get("level_index")), row.getOrDefault("level_name", "")));
        }
    }

    private void parseTrips(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String tripId = row.getOrDefault("trip_id", "");
            String routeId = row.getOrDefault("route_id", "");
            if (tripId.isEmpty() || routeId.isEmpty()) continue;
            trips.put(tripId, new Trip(routeId, //
                    row.getOrDefault("direction_id", ""), row.getOrDefault("shape_id", ""), //
                    row.getOrDefault("wheelchair_accessible", "")));
        }
    }

    /**
     * Streams stop_times.txt. Rows of one trip are expected to be contiguous (GTFS best practice); each contiguous
     * run is sorted by stop_sequence and converted to directed travel edges of the trip's route subgraph.
     * Non-contiguous trips still produce correct edges per run, but are counted and reported.
     */
    private void parseStopTimes(Reader reader) throws IOException {
        GtfsCsvReader csv = new GtfsCsvReader(reader);
        Set<String> completedTrips = new HashSet<>();
        String currentTrip = null;
        List<long[]> run = new ArrayList<>(); // [stop_sequence, index into runStopIds]
        List<String> runStopIds = new ArrayList<>();

        Map<String, String> row;
        while ((row = csv.nextRow()) != null) {
            String tripId = row.getOrDefault("trip_id", "");
            String stopId = row.getOrDefault("stop_id", "");
            String seqString = row.getOrDefault("stop_sequence", "");
            if (tripId.isEmpty() || stopId.isEmpty()) continue;
            if (!tripId.equals(currentTrip)) {
                flushRun(currentTrip, run, runStopIds);
                if (currentTrip != null) completedTrips.add(currentTrip);
                if (completedTrips.contains(tripId)) nonContiguousTrips++;
                currentTrip = tripId;
                run.clear();
                runStopIds.clear();
            }
            long seq;
            try {
                seq = Long.parseLong(seqString);
            } catch (NumberFormatException e) {
                badSequenceValues++;
                continue;
            }
            run.add(new long[]{seq, runStopIds.size()});
            runStopIds.add(stopId);
        }
        flushRun(currentTrip, run, runStopIds);
    }

    private void flushRun(@Nullable String tripId, List<long[]> run, List<String> runStopIds) {
        if (tripId == null || run.isEmpty()) return;
        Trip trip = trips.get(tripId);
        if (trip == null) {
            unknownTripRefs++;
            return;
        }

        run.sort(Comparator.comparingLong(a -> a[0]));
        List<String> platforms = new ArrayList<>(run.size());
        for (long[] entry : run) {
            String stopId = runStopIds.get((int) entry[1]);
            Stop stop = stops.get(stopId);
            if (stop == null || !stop.isEmittableLocation()) {
                unknownStopRefs++;
                continue;
            }
            // collapse a stop repeated back-to-back in the same trip
            if (!platforms.isEmpty() && platforms.getLast().equals(stopId)) continue;
            platforms.add(stopId);
        }

        Map<String, TravelEdge> edges = routeEdges.computeIfAbsent(trip.routeId(), k -> new TreeMap<>());
        for (int i = 0; i + 1 < platforms.size(); i++) {
            String key = platforms.get(i) + "\n" + platforms.get(i + 1);
            TravelEdge edge = edges.computeIfAbsent(key, k -> new TravelEdge());
            edge.tripCount++;
            if ("1".equals(trip.wheelchairAccessible())) edge.wheelchairAccessibleTripCount++;
            if (!trip.shapeId().isEmpty()) edge.shapeIds.add(trip.shapeId());
            if (!trip.directionId().isEmpty()) edge.directionIds.add(trip.directionId());
        }
    }

    // ---------------------------------------------------------------- emitting

    private void emit(ICjStream cj) {
        IJsonFactory json = cj.jsonFactory();

        ICjDocumentChunkMutable document = cj.createDocumentChunk();
        document.dataMutable(data -> {
            data.add("format", "gtfs");
            data.add("source", files.name());
            if (!agencyNames.isEmpty()) {
                data.add("agencies", stringArray(json, new TreeSet<>(agencyNames.values())));
            }
        });
        cj.documentStart(document);

        emitBaseGraph(cj, json);
        emitRouteGraphs(cj, json);

        cj.documentEnd();
    }

    /** Level 1: stations, platforms, entrances and in-station nodes; transfer/pathway/synthesized edges. */
    private void emitBaseGraph(ICjStream cj, IJsonFactory json) {
        ICjGraphChunkMutable base = cj.createGraphChunk();
        base.id(BASE_GRAPH_ID);
        base.addLabelWithoutLanguage("Physical network: stations, platforms, transfers, pathways");
        cj.graphStart(base);

        for (Map.Entry<String, Stop> entry : new TreeMap<>(stops).entrySet()) {
            emitStopNode(cj, entry.getKey(), entry.getValue());
        }
        emitTransferEdges(cj);
        emitPathwayEdges(cj);
        emitSynthesizedTransferEdges(cj);

        cj.graphEnd();
    }

    private void emitStopNode(ICjStream cj, String stopId, Stop stop) {
        if (!stop.isEmittableLocation()) return;
        ICjNodeChunkMutable node = cj.createNodeChunk();
        node.id(stopId);
        node.addLabelWithoutLanguage(stop.name());
        node.dataMutable(data -> {
            if (stop.lat() != null && stop.lon() != null) {
                data.add("lat", stop.lat());
                data.add("lon", stop.lon());
            }
            data.add("locationType", stop.locationType().isEmpty() ? "0" : stop.locationType());
            if (!stop.parentId().isEmpty()) data.add("parentStation", stop.parentId());
            if (!stop.platformCode().isEmpty()) data.add("platformCode", stop.platformCode());
            Level level = levels.get(stop.levelId());
            if (level != null) {
                if (level.index() != null) data.add("level", level.index());
                if (!level.name().isEmpty()) data.add("levelName", level.name());
            }
            Boolean wheelchair = resolveWheelchairBoarding(stop);
            if (wheelchair != null) data.add("isWheelchairAccessible", wheelchair);
        });
        cj.node(node);
    }

    /**
     * GTFS wheelchair_boarding semantics: 1 = accessible, 2 = not accessible, 0/empty = no information -
     * except for child locations, where 0/empty means "inherit from the parent station".
     *
     * @return true/false, or null if unknown
     */
    private @Nullable Boolean resolveWheelchairBoarding(Stop stop) {
        Boolean own = wheelchairValue(stop.wheelchairBoarding());
        if (own != null) return own;
        if (!stop.parentId().isEmpty()) {
            Stop parent = stops.get(stop.parentId());
            if (parent != null) return wheelchairValue(parent.wheelchairBoarding());
        }
        return null;
    }

    private static @Nullable Boolean wheelchairValue(String gtfsValue) {
        return switch (gtfsValue) {
            case "1" -> Boolean.TRUE;
            case "2" -> Boolean.FALSE;
            default -> null;
        };
    }

    /** Tier 2: explicit transfers, with the route/trip-conditional v2 fields as data. */
    private void emitTransferEdges(ICjStream cj) {
        for (Transfer transfer : transfers) {
            if (!isEmittedStop(transfer.fromStopId()) || !isEmittedStop(transfer.toStopId())) {
                unknownTransferStopRefs++;
                continue;
            }
            markCoveredComplex(transfer.fromStopId(), transfer.toStopId());
            ICjEdgeChunkMutable edge = cj.createEdgeChunk();
            edge.edgeType(EDGE_TYPE_TRANSFER);
            edge.addEndpoint(ep -> ep.node(transfer.fromStopId()).direction(CjDirection.IN));
            edge.addEndpoint(ep -> ep.node(transfer.toStopId()).direction(CjDirection.OUT));
            edge.dataMutable(data -> {
                if (!transfer.transferType().isEmpty()) data.add("transferType", transfer.transferType());
                Double minTransferTime = parseDoubleOrNull(transfer.minTransferTime());
                if (minTransferTime != null) data.add("minTransferTime", minTransferTime);
                if (!transfer.fromRouteId().isEmpty()) data.add("fromRouteId", transfer.fromRouteId());
                if (!transfer.toRouteId().isEmpty()) data.add("toRouteId", transfer.toRouteId());
                if (!transfer.fromTripId().isEmpty()) data.add("fromTripId", transfer.fromTripId());
                if (!transfer.toTripId().isEmpty()) data.add("toTripId", transfer.toTripId());
            });
            cj.edge(edge);
        }
        if (unknownTransferStopRefs > 0) {
            sendIssue(ContentError.ErrorLevel.Warn, //
                    unknownTransferStopRefs + " transfers reference unknown stop_ids and were skipped");
        }
    }

    /** Tier 1: the pathway network. Bidirectional pathways become two directed edges. */
    private void emitPathwayEdges(ICjStream cj) {
        for (Pathway pathway : pathways) {
            if (!isEmittedStop(pathway.fromStopId()) || !isEmittedStop(pathway.toStopId())) {
                unknownPathwayStopRefs++;
                continue;
            }
            markCoveredComplex(pathway.fromStopId(), pathway.toStopId());
            emitPathwayEdge(cj, pathway, false);
            if (pathway.bidirectional()) emitPathwayEdge(cj, pathway, true);
        }
        if (unknownPathwayStopRefs > 0) {
            sendIssue(ContentError.ErrorLevel.Warn, //
                    unknownPathwayStopRefs + " pathways reference unknown stop_ids and were skipped");
        }
    }

    private void emitPathwayEdge(ICjStream cj, Pathway pathway, boolean reversed) {
        String from = reversed ? pathway.toStopId() : pathway.fromStopId();
        String to = reversed ? pathway.fromStopId() : pathway.toStopId();
        ICjEdgeChunkMutable edge = cj.createEdgeChunk();
        edge.edgeType(PATHWAY_MODES.getOrDefault(pathway.mode(), "pathway"));
        edge.addEndpoint(ep -> ep.node(from).direction(CjDirection.IN));
        edge.addEndpoint(ep -> ep.node(to).direction(CjDirection.OUT));
        edge.dataMutable(data -> {
            if (!pathway.id().isEmpty()) data.add("pathwayId", pathway.id());
            if (pathway.traversalTime() != null) data.add("traversalTime", pathway.traversalTime());
            if (pathway.length() != null) data.add("length", pathway.length());
            if (pathway.stairCount() != null) data.add("stairCount", pathway.stairCount());
            if (pathway.maxSlope() != null) data.add("maxSlope", pathway.maxSlope());
            if (pathway.minWidth() != null) data.add("minWidth", pathway.minWidth());
            String signposted = reversed ? pathway.reversedSignpostedAs() : pathway.signpostedAs();
            if (!signposted.isEmpty()) data.add("signpostedAs", signposted);
            // derived accessibility policy: stairs and escalators are not wheelchair-accessible, everything
            // else is; consumers needing stricter rules can re-derive from the raw minWidth/maxSlope fields
            if (!pathway.mode().isEmpty()) {
                data.add("isWheelchairAccessible", //
                        !PATHWAY_MODES_NOT_WHEELCHAIR_ACCESSIBLE.contains(pathway.mode()));
            }
        });
        cj.edge(edge);
    }

    /**
     * Tier 3: a station with two or more platforms but no intra-station pathway or transfer gets undirected
     * platform-platform edges, flagged {@code synthesized=true} - guessed connectivity, not measured.
     */
    private void emitSynthesizedTransferEdges(ICjStream cj) {
        Map<String, List<String>> stationPlatforms = new TreeMap<>();
        for (Map.Entry<String, Stop> entry : new TreeMap<>(stops).entrySet()) {
            Stop stop = entry.getValue();
            if (stop.isPlatform() && !stop.parentId().isEmpty()) {
                stationPlatforms.computeIfAbsent(stop.parentId(), k -> new ArrayList<>()).add(entry.getKey());
            }
        }
        for (Map.Entry<String, List<String>> entry : stationPlatforms.entrySet()) {
            List<String> platforms = entry.getValue();
            if (platforms.size() < 2 || coveredComplexes.contains(entry.getKey())) continue;
            for (int i = 0; i < platforms.size(); i++) {
                for (int j = i + 1; j < platforms.size(); j++) {
                    String a = platforms.get(i);
                    String b = platforms.get(j);
                    ICjEdgeChunkMutable edge = cj.createEdgeChunk();
                    edge.edgeType(EDGE_TYPE_TRANSFER);
                    edge.addEndpoint(ep -> ep.node(a).direction(CjDirection.UNDIR));
                    edge.addEndpoint(ep -> ep.node(b).direction(CjDirection.UNDIR));
                    edge.dataMutable(data -> data.add("synthesized", true));
                    cj.edge(edge);
                }
            }
        }
    }

    private void markCoveredComplex(String fromStopId, String toStopId) {
        Stop from = stops.get(fromStopId);
        Stop to = stops.get(toStopId);
        if (from == null || to == null) return;
        String fromComplex = from.complexId(fromStopId);
        if (fromComplex.equals(to.complexId(toStopId))) {
            coveredComplexes.add(fromComplex);
        }
    }

    /** Level 2: one graph per route; edges only, referencing the base graph's platform nodes by id. */
    private void emitRouteGraphs(ICjStream cj, IJsonFactory json) {
        List<String> routeIds = new ArrayList<>(routeEdges.keySet());
        routeIds.sort(Comparator.comparing((String id) -> { //
            Route route = routes.get(id);
            return route == null ? id : route.displayName(id);
        }).thenComparing(id -> id));

        for (String routeId : routeIds) {
            Route route = routes.get(routeId);
            ICjGraphChunkMutable graph = cj.createGraphChunk();
            graph.id(ROUTE_GRAPH_ID_PREFIX + routeId);
            graph.addLabelWithoutLanguage(route == null ? routeId : route.displayName(routeId));
            graph.dataMutable(data -> {
                data.add("gtfsRouteId", routeId);
                if (route != null) {
                    if (!route.shortName().isEmpty()) data.add("routeShortName", route.shortName());
                    if (!route.longName().isEmpty()) data.add("routeLongName", route.longName());
                    if (!route.type().isEmpty()) data.add("routeType", route.type());
                    if (!route.color().isEmpty()) data.add("routeColor", route.color());
                    String agencyName = agencyNames.get(route.agencyId());
                    if (agencyName != null) data.add("agency", agencyName);
                }
            });
            cj.graphStart(graph);

            for (Map.Entry<String, TravelEdge> entry : routeEdges.get(routeId).entrySet()) {
                String[] fromTo = entry.getKey().split("\n");
                TravelEdge travelEdge = entry.getValue();
                ICjEdgeChunkMutable edge = cj.createEdgeChunk();
                edge.addEndpoint(ep -> ep.node(fromTo[0]).direction(CjDirection.IN));
                edge.addEndpoint(ep -> ep.node(fromTo[1]).direction(CjDirection.OUT));
                edge.dataMutable(data -> {
                    data.add("tripCount", travelEdge.tripCount);
                    if (travelEdge.wheelchairAccessibleTripCount > 0) {
                        data.add("wheelchairAccessibleTripCount", travelEdge.wheelchairAccessibleTripCount);
                    }
                    if (!travelEdge.shapeIds.isEmpty()) data.add("shapeIds", stringArray(json, travelEdge.shapeIds));
                    if (!travelEdge.directionIds.isEmpty())
                        data.add("directionIds", stringArray(json, travelEdge.directionIds));
                });
                cj.edge(edge);
            }

            cj.graphEnd();
        }
    }

    private boolean isEmittedStop(String stopId) {
        Stop stop = stops.get(stopId);
        return stop != null && stop.isEmittableLocation();
    }

    private static IJsonArrayMutable stringArray(IJsonFactory json, Iterable<String> strings) {
        IJsonArrayMutable array = json.createArrayMutable();
        for (String s : strings) {
            array.add(json.createString(s));
        }
        return array;
    }

    // ---------------------------------------------------------------- errors

    private boolean missingRequiredFile(String fileName) {
        sendIssue(ContentError.ErrorLevel.Error, "GTFS feed '" + files.name() + "' is missing required file " + fileName);
        return false;
    }

    private void reportSummaryWarnings() {
        if (unknownStopRefs > 0)
            sendIssue(ContentError.ErrorLevel.Warn, unknownStopRefs + " stop_times rows reference unknown or non-platform stop_ids");
        if (unknownTripRefs > 0)
            sendIssue(ContentError.ErrorLevel.Warn, unknownTripRefs + " stop_times trips reference unknown trip_ids");
        if (badSequenceValues > 0)
            sendIssue(ContentError.ErrorLevel.Warn, badSequenceValues + " stop_times rows have a non-numeric stop_sequence");
        if (nonContiguousTrips > 0)
            sendIssue(ContentError.ErrorLevel.Warn, nonContiguousTrips + " trips have non-contiguous rows in stop_times.txt");
        if (routeEdges.isEmpty())
            sendIssue(ContentError.ErrorLevel.Warn, "GTFS feed contains no usable stop_times; no route subgraphs");
    }

    private void sendIssue(ContentError.ErrorLevel level, String message) {
        Nullables.ifConsumerPresentAccept(errorHandler, ContentError.of(level, message));
    }

    private static @Nullable Double parseDoubleOrNull(@Nullable String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
