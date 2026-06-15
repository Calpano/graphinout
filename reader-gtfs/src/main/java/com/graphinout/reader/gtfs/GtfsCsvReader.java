package com.graphinout.reader.gtfs;

import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal streaming CSV reader for GTFS files (RFC 4180 dialect as used by GTFS).
 * <ul>
 *     <li>first record is the header; {@link #nextRow()} returns column-name → value maps</li>
 *     <li>handles quoted fields with embedded commas, escaped quotes ({@code ""}) and embedded line breaks</li>
 *     <li>accepts LF and CRLF line endings, skips a leading UTF-8 BOM, skips blank records</li>
 * </ul>
 * Values are returned trimmed; missing trailing columns map to {@code ""}.
 */
public class GtfsCsvReader implements Closeable {

    private static final char BOM = '\uFEFF';

    private final Reader reader;
    private final List<String> header;
    private long recordNumber = 1; // header was record 1
    private boolean eof = false;
    private boolean bomCandidate = true;

    public GtfsCsvReader(Reader reader) throws IOException {
        this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
        List<String> headerRecord = nextRecord();
        if (headerRecord == null) {
            throw new IOException("CSV input is empty, expected a header record");
        }
        this.header = headerRecord.stream().map(String::trim).toList();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public List<String> header() {
        return header;
    }

    /**
     * @return the next row as map of header-name → trimmed value, or null at end of input
     */
    public @Nullable Map<String, String> nextRow() throws IOException {
        List<String> record = nextRecord();
        if (record == null) return null;
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < header.size(); i++) {
            String value = i < record.size() ? record.get(i).trim() : "";
            row.put(header.get(i), value);
        }
        return row;
    }

    /**
     * 1-based record number of the record returned by the last {@link #nextRow()} call. Useful for error reporting;
     * for files without embedded line breaks this equals the line number.
     */
    public long recordNumber() {
        return recordNumber;
    }

    private @Nullable List<String> nextRecord() throws IOException {
        if (eof) return null;
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean sawAnyChar = false;
        while (true) {
            int ci = reader.read();
            if (ci == -1) {
                eof = true;
                if (!sawAnyChar) return null;
                fields.add(field.toString());
                recordNumber++;
                return fields;
            }
            char c = (char) ci;
            if (bomCandidate) {
                bomCandidate = false;
                if (c == BOM) continue;
            }
            if (inQuotes) {
                if (c == '"') {
                    int next = reader.read();
                    if (next == '"') {
                        field.append('"');
                    } else if (next == -1) {
                        eof = true;
                        fields.add(field.toString());
                        recordNumber++;
                        return fields;
                    } else {
                        inQuotes = false;
                        char n = (char) next;
                        if (n == ',') {
                            fields.add(field.toString());
                            field.setLength(0);
                        } else if (n == '\n' || n == '\r') {
                            if (n == '\r') maybeConsumeLf();
                            fields.add(field.toString());
                            recordNumber++;
                            return fields;
                        } else {
                            // malformed: stray char after closing quote; keep it as content
                            field.append(n);
                        }
                    }
                } else {
                    field.append(c);
                }
            } else {
                switch (c) {
                    case '"' -> {
                        inQuotes = true;
                        sawAnyChar = true;
                    }
                    case ',' -> {
                        fields.add(field.toString());
                        field.setLength(0);
                        sawAnyChar = true;
                    }
                    case '\r', '\n' -> {
                        if (c == '\r') maybeConsumeLf();
                        if (!sawAnyChar) continue; // blank line: keep scanning
                        fields.add(field.toString());
                        recordNumber++;
                        return fields;
                    }
                    default -> {
                        field.append(c);
                        sawAnyChar = true;
                    }
                }
            }
        }
    }

    private void maybeConsumeLf() throws IOException {
        reader.mark(1);
        int next = reader.read();
        if (next != '\n' && next != -1) {
            reader.reset();
        }
    }
}
