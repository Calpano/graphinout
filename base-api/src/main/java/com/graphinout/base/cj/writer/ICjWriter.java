package com.graphinout.base.cj.writer;

import com.graphinout.base.cj.CjException;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.CjType;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.foundation.pure.input.IHandleContentErrors;
import com.graphinout.foundation.pure.json.writer.IJsonXmlStringWriter;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
 * regardless, the {@link ICjWriter} has the same structure:
 * <ol>
 *     <li>{@link ICjWriter#documentStart() CJ document start}</li>
 *     <li>{@link #graphStart() CJ graph start}, ... graph object 1..., {@link ICjWriter#graphEnd() CJ graph end}</li>
 *     <li>If more graphs are present: <li>{@link #graphStart() CJ graph start}, ... graph object i..., {@link ICjWriter#graphEnd() CJ graph end}</li>
 *     <li>{@link ICjWriter#documentEnd() CJ document end}</li>
 * </ol>
 */
public interface ICjWriter extends JsonWriter, IHasCjWriter, IJsonXmlStringWriter, IHandleContentErrors {

    /** Document-level {@code @context} namespace map for URI expansion */
    void context(Map<String, String> context);

    default ICjWriter cjWriter() {
        return this;
    }

    /**
     * Signals the end of the root connected JSON declaration.
     */
    void connectedJsonEnd();

    /**
     * Signals the start of the root connected JSON declaration.
     */
    void connectedJsonStart();

    /**
     * Sets the canonical flag for the connected JSON context.
     *
     * @param b true if canonical, false otherwise.
     */
    void connectedJson__canonical(boolean b);

    /**
     * Sets the version date for the connected JSON context.
     *
     * @param s the version date string.
     */
    void connectedJson__versionDate(String s);

    /**
     * Sets the version number for the connected JSON context.
     *
     * @param s the version number string.
     */
    void connectedJson__versionNumber(String s);

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
    void edgeType(ICjElementType edgeType);

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

    /**
     * Sets the ID of the current CJ element (e.g., node, edge, graph).
     *
     * @param id the element ID.
     */
    void id(String id);

    /** Marker for extension data end. */
    void jsonDataEnd();

    /** Marker for extension data start. */
    void jsonDataStart();

    /**
     * End a label object. Implementations should close the label object structure.
     */
    void labelEnd();

    /**
     * Signals the end of a label entry within a label object.
     */
    void labelEntryEnd();

    /**
     * Signals the start of a new label entry within a label object.
     */
    void labelEntryStart();

    /**
     * Start a label object. Implementations should write the "label" key and start the object structure.
     */
    void labelStart();

    /**
     * Sets the language of the current label entry.
     *
     * @param language the language code.
     */
    void language(String language);

    default <T> void list(List<T> list, CjType cjArrayType, boolean sort, BiConsumer<T, ICjWriter> element_writer) {
        if (!list.isEmpty()) {
            listStart(cjArrayType);
            if (sort) {
                list.stream().sorted().forEach(x -> element_writer.accept(x, this));
            } else {
                list.forEach(x -> element_writer.accept(x, this));
            }
            listEnd(cjArrayType);
        }
    }

    /**
     * Signals the end of a CJ list of the specified type.
     *
     * @param cjType the type of elements in the list.
     */
    void listEnd(CjType cjType);

    /**
     * Signals the start of a CJ list of the specified type.
     *
     * @param cjType the type of elements in the list.
     */
    void listStart(CjType cjType);

    default <T> void maybe(@Nullable T object, Consumer<@NonNull T> consumer) {
        if (object != null) {
            consumer.accept(object);
        }
    }

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

    /** node.types[] - node type URI/string */
    void nodeType(ICjElementType nodeType);

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

    /**
     * Sets the value for the current element, typically a label entry.
     *
     * @param value the string value.
     */
    void value(String value);

}
