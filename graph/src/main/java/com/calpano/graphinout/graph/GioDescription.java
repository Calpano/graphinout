package com.calpano.graphinout.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rbaba
 * @version 0.0.1
 *  Provides human-readable descriptions for the GraphML element containing this <desc> as its first child.
 *  Occurence: <key>, <graphml>, <graph>, <node>, <port>, <edge>, <hyperedge>, and <endpoint>.
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GioDescription {
    private String value;
}
