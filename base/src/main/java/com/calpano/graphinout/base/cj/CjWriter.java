package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.foundation.json.JsonWriter;

/**
 * This API embeds stream of conceptual opening and closing tags (read/write events) for <em>connected JSON</em> into a
 * stream of JSON events.
 * <p>
 * Contract: Any structure in JSON which is interpreted by CJ is NOT emitted as JSON events, but as CJ events. Example:
 * The JSON document contains either a
 * <pre>
 *     {
 *         "graph": { ... graph object ... }   // object
 *     }
 * </pre>
 * which is in {@link JsonWriter}
 * <ol>
 *     <li>{@link JsonWriter#documentStart() JSON document start}</li>
 *     <li>{@link JsonWriter#objectStart() JSON object start}</li>
 *     <li>{@link JsonWriter#onKey(String) JSON object key "graph"}</li>
 *     <li>{@link JsonWriter#objectStart() JSON object start}, ... graph object 1..., {@link JsonWriter#objectEnd() JSON object end}</li>
 *     <li>{@link JsonWriter#objectEnd() JSON object end}</li>
 *     <li>{@link JsonWriter#documentEnd() JSON document end}</li>
 * </ol>
 * <p>
 * or
 * <pre>
 *     {
 *         "graph": [{ ... graph object 1 ... }, { ... graph object 2 ... }] // array
 *     }
 * </pre>
 * which is in {@link JsonWriter}
 * <ol>
 *     <li>{@link JsonWriter#documentStart() JSON document start}</li>
 *     <li>{@link JsonWriter#objectStart() JSON object start}</li>
 *     <li>{@link JsonWriter#onKey(String) JSON object key "graph"}</li>
 *     <li>{@link JsonWriter#arrayStart() JSON array start}</li>
 *     <li>{@link JsonWriter#objectStart() JSON object start}, ... graph object 1..., {@link JsonWriter#objectEnd() JSON object end}</li>
 *     <li>{@link JsonWriter#objectStart() JSON object start}, ... graph object 2..., {@link JsonWriter#objectEnd() JSON object end}</li>
 *     <li>{@link JsonWriter#arrayEnd() JSON array end}</li>
 *     <li>{@link JsonWriter#objectEnd() JSON object end}</li>
 *     <li>{@link JsonWriter#documentEnd() JSON document end}</li>
 * </ol>
 * <p>
 * regardless, the {@link CjWriter} has the same structure:
 * <ol>
 *     <li>{@link CjWriter#documentStart() CJ document start}</li>
 *     <li>{@link #graphStart() CJ graph start}, ... graph object 1..., {@link CjWriter#graphEnd() CJ graph end}</li>
 *     <li>If more graphs are present: <li>{@link #graphStart() CJ graph start}, ... graph object i..., {@link CjWriter#graphEnd() CJ graph end}</li>
 *     <li>{@link CjWriter#documentEnd() CJ document end}</li>
 * </ol>
 */
public interface CjWriter extends JsonWriter {


    /** Graph base uri */
    void baseUri(String baseUri);

    /** endpoint.direction */
    void direction(CjDirection direction);

    /**
     * CJ Edge end event.
     */
    void edgeEnd();

    /**
     * CJ Edge start event.
     * <p>
     * Sub-elements:
     * <ul>
     *   <li>{@link #graphStart() Graph} - nested graphs within the edge</li>
     *   <li>{@link #endpointStart() Endpoint} - edge endpoints connecting to nodes/ports</li>
     * </ul>
     */
    void edgeStart();

    /** edge.type / endpoint.type */
    void edgeType(CjEdgeType edgeType);

    /**
     * CJ Endpoint end event.
     */
    void endpointEnd();

    /**
     * CJ Endpoint start event.
     * <p>
     * Sub-elements: None (leaf element)
     */
    void endpointStart();

    /**
     * CJ Graph end event.
     */
    void graphEnd() throws CjException;

    /**
     * CJ Graph start event.
     * <p>
     * Sub-elements:
     * <ul>
     *   <li>{@link #nodeStart() Node} - nodes within the graph</li>
     *   <li>{@link #edgeStart() Edge} - edges within the graph</li>
     * </ul>
     */
    void graphStart() throws CjException;

    void id(String id);

    /** Marker for extension data end. */
    void jsonEnd();

    /** Marker for extension data start. */
    void jsonStart();

    void labelEnd();

    void labelEntryEnd();

    void labelEntryStart();

    void labelStart();

    void language(String language);

    /**
     * CJ Node end event.
     */
    void nodeEnd();

    /** endpoint.node */
    void nodeId(String nodeId);

    /**
     * CJ Node start event.
     * <p>
     * Sub-elements:
     * <ul>
     *   <li>{@link #graphStart() Graph} - nested graphs within the node (compound nodes)</li>
     *   <li>{@link #portStart() Port} - ports attached to the node</li>
     * </ul>
     */
    void nodeStart();

    /**
     * CJ Port end event.
     */
    void portEnd();

    /** endpoint.port */
    void portId(String portId);

    /**
     * CJ Port start event.
     * <p>
     * Sub-elements:
     * <ul>
     *   <li>{@link #portStart() Port} - nested sub-ports (hierarchical ports)</li>
     * </ul>
     */
    void portStart();

    void value(String value);

    void listStart(CjType cjType);
    void listEnd(CjType cjType);

}
