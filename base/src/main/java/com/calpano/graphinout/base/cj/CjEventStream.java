package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.json.JsonEventStream;
import edu.umd.cs.findbugs.annotations.Nullable;

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
 * which is in {@link JsonEventStream}
 * <ol>
 *     <li>{@link JsonEventStream#documentStart() JSON document start}</li>
 *     <li>{@link JsonEventStream#objectStart() JSON object start}</li>
 *     <li>{@link JsonEventStream#onKey(String) JSON object key "graph"}</li>
 *     <li>{@link JsonEventStream#objectStart() JSON object start}, ... graph object 1..., {@link JsonEventStream#objectEnd() JSON object end}</li>
 *     <li>{@link JsonEventStream#objectEnd() JSON object end}</li>
 *     <li>{@link JsonEventStream#documentEnd() JSON document end}</li>
 * </ol>
 * <p>
 * or
 * <pre>
 *     {
 *         "graph": [{ ... graph object 1 ... }, { ... graph object 2 ... }] // array
 *     }
 * </pre>
 * which is in {@link JsonEventStream}
 * <ol>
 *     <li>{@link JsonEventStream#documentStart() JSON document start}</li>
 *     <li>{@link JsonEventStream#objectStart() JSON object start}</li>
 *     <li>{@link JsonEventStream#onKey(String) JSON object key "graph"}</li>
 *     <li>{@link JsonEventStream#arrayStart() JSON array start}</li>
 *     <li>{@link JsonEventStream#objectStart() JSON object start}, ... graph object 1..., {@link JsonEventStream#objectEnd() JSON object end}</li>
 *     <li>{@link JsonEventStream#objectStart() JSON object start}, ... graph object 2..., {@link JsonEventStream#objectEnd() JSON object end}</li>
 *     <li>{@link JsonEventStream#arrayEnd() JSON array end}</li>
 *     <li>{@link JsonEventStream#objectEnd() JSON object end}</li>
 *     <li>{@link JsonEventStream#documentEnd() JSON document end}</li>
 * </ol>
 * <p>
 * regardless, the {@link CjEventStream} has the same structure:
 * <ol>
 *     <li>{@link CjEventStream#documentStart() CJ document start}</li>
 *     <li>{@link CjEventStream#graphStart(CjGraph) CJ graph start}, ... graph object 1..., {@link CjEventStream#graphEnd() CJ graph end}</li>
 *     <li>If more graphs are present: <li>{@link CjEventStream#graphStart(CjGraph) CJ graph start}, ... graph object i..., {@link CjEventStream#graphEnd() CJ graph end}</li>
 *     <li>{@link CjEventStream#documentEnd() CJ document end}</li>
 * </ol>
 */
public interface CjEventStream extends JsonEventStream {

    interface CjGraph {

        @Nullable
        String baseuri();

        @Nullable
        String edgedefault();

        @Nullable
        String id();

        @Nullable
        String label();

    }

    interface CjNode {

        @Nullable
        String id();

        @Nullable
        String label();

    }

    interface CjPort {

        @Nullable
        String id();

        @Nullable
        String label();

    }

    interface CjEdge {

    }

    interface CjEndpoint {

    }

    void edgeEnd();

    /** Sub-elements: {@link #graphStart(CjGraph) graph}, {@link #endpointStart(CjEndpoint) endpoint} */
    void edgeStart(CjEdge edge);

    void endpointEnd();

    void endpointStart(CjEndpoint endpoint);

    /**
     * CJ Graph
     */
    void graphEnd() throws CjException;

    /**
     * CJ Graph
     */
    void graphStart(CjGraph graph) throws CjException;

    void nodeEnd();

    /**
     * Sub-elements: {@link #graphStart(CjGraph) graph}, {@link #portStart(CjPort) port}
     */
    void nodeStart(CjNode node);

    void portEnd();

    /** Sub-elements: {@link #portStart(CjPort) port} */
    void portStart(CjPort port);


}
