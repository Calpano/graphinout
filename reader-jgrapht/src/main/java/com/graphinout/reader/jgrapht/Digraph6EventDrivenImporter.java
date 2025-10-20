package com.graphinout.reader.jgrapht;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.nio.BaseEventDrivenImporter;
import org.jgrapht.nio.EventDrivenImporter;
import org.jgrapht.nio.ImportException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom importer for digraph6 format (directed graphs).
 * Based on the nauty digraph6 specification.
 */
public class Digraph6EventDrivenImporter extends BaseEventDrivenImporter<Integer, Pair<Integer, Integer>> implements EventDrivenImporter<Integer, Pair<Integer, Integer>> {

    public void importInput(Reader input) throws ImportException {
        try (BufferedReader br = new BufferedReader(input)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(">>")) {
                    continue; // Skip empty lines and comments
                }
                parseLine(line);
            }
        } catch (IOException e) {
            throw new ImportException("Failed to parse digraph6 format", e);
        }
    }

    private void parseLine(String line) {
        if (!line.startsWith("&")) {
            throw new ImportException("Invalid digraph6 format: must start with '&'");
        }
        
        // Remove the '&' prefix
        String data = line.substring(1);
        
        // Parse number of vertices
        int pos = 0;
        int n;
        if (data.isEmpty()) {
            throw new ImportException("Invalid digraph6 format: empty data after '&'");
        }
        
        char first = data.charAt(pos);
        if (first == '~') {
            // Large graph format (not commonly used, skip for now)
            throw new ImportException("Large digraph6 format not yet supported");
        } else {
            n = (int) first - 63;
            pos++;
        }
        
        // Notify vertices
        for (int i = 0; i < n; i++) {
            notifyVertex(i);
        }
        
        // Parse adjacency matrix (all n*n entries)
        List<Boolean> bits = new ArrayList<>();
        for (int i = pos; i < data.length(); i++) {
            char c = data.charAt(i);
            int value = (int) c - 63;
            for (int j = 5; j >= 0; j--) {
                bits.add((value & (1 << j)) != 0);
            }
        }
        
        // Read edges from adjacency matrix
        int bitIndex = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (bitIndex < bits.size() && bits.get(bitIndex)) {
                    notifyEdge(Pair.of(i, j));
                }
                bitIndex++;
            }
        }
    }
}
