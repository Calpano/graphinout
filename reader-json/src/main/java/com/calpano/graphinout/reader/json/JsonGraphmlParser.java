package com.calpano.graphinout.reader.json;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioEndpointDirection;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.reader.json.mapper.PathBuilder;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonGraphmlParser {
    private final SingleInputSource inputSource;
    private final GioWriter writer;
    private final PathBuilder pathBuilder;


    private final ReadContext ctx;

    public JsonGraphmlParser(InputSource inputSource, GioWriter writer, PathBuilder pathBuilder) throws IOException {
        this.inputSource = (SingleInputSource) inputSource;
        this.writer = writer;
        this.pathBuilder = pathBuilder;
        // IMPROVE it would suffice to read only 1 object at a time
        ctx = JsonPath.parse(this.inputSource.inputStream());
    }

    /**
     *
     * @throws IOException when writing fails
     */
    public void read() throws IOException {
        writer.startDocument(GioDocument.builder().build());
        writer.startGraph(GioGraph.builder().build());
        long countOfNode = ctx.read("$.length()", Long.class);

        List<Integer> nodes = ctx.read(pathBuilder.findAllId().path);
        for (Integer s : nodes) {

            writer.startNode(GioNode.builder().id(s.toString()).build());
            writer.endNode(null);
        }

        for (Integer s : nodes) {

//            for (String format : pathBuilder.findLink(s)) {
//                List<List<Integer>> endpoints = ctx.read(String.format(format, s));
//                List<GioEndpoint> gioEndpoints = new ArrayList<>();
//                if (endpoints.size() > 0) {
//                    gioEndpoints.add(GioEndpoint.builder().id(s.toString()).type(GioEndpointDirection.Out).build());
//                    endpoints.get(0).forEach(endpoint -> gioEndpoints.add(GioEndpoint.builder().node(endpoint.toString()).type(GioEndpointDirection.In).build()));
//                }
//                if (gioEndpoints.size() > 0) {
//                    writer.startEdge(GioEdge.builder().endpoints(gioEndpoints).build());
//                    writer.endEdge();
//                }
//            }
        }
        writer.endGraph(null);
        writer.endDocument();


    }
}
