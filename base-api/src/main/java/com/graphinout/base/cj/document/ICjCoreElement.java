package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.graphinout.base.cj.document.CjUris.BLANK_NODE_PSEUDO_SCHEME;

/**
 * A common super-interface for {@link ICjGraph}, {@link ICjNode} and {@link ICjEdge}.
 */
public interface ICjCoreElement extends ICjHasId, ICjHasUri, ICjHasGraphs, ICjElement {

    class ParentIterator implements Iterator<ICjElement> {

        private ICjElement current;

        public ParentIterator(@NonNull ICjElement start) {
            this.current = start;
            assert current instanceof ICjCoreElement || current instanceof ICjDocument : "got " + start.getClass().getSimpleName();
        }

        public boolean hasNext() {
            return current instanceof ICjGraph;
        }

        public ICjElement next() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            ICjElement result = current;
            if (current instanceof ICjCoreElement coreElement) {
                current = coreElement.parent();
            } else if (current instanceof ICjDocument document) {
                // For a document, the parent is null, so we stop here.
                current = null;
            } else {
                throw new AssertionError();
            }
            return result;
        }

    }

    static int findPositionOfChildInParent(ICjElement parent, ICjElement child) {
        // Cases:
        // doc-graph
        // graph-graph
        // graph-node
        // graph-edge
        // edge-graph
        // node-graph
        switch (parent.cjType()) {
            case RootObject -> {
                assert child instanceof ICjGraph;
                return ((ICjDocument) parent).indexOf((ICjGraph) child);
            }
            case Graph -> {
                switch (child.cjType()) {
                    case Graph -> {
                        assert child instanceof ICjGraph;
                        return ((ICjGraph) parent).indexOf((ICjGraph) child);
                    }
                    case Node -> {
                        assert child instanceof ICjGraph;
                        return ((ICjNode) parent).indexOf((ICjGraph) child);
                    }
                    case Edge -> {
                        assert child instanceof ICjGraph;
                        return ((ICjEdge) parent).indexOf((ICjGraph) child);
                    }
                    default -> throw new AssertionError();
                }
            }
            case Node -> {
                assert child instanceof ICjGraph;
                return ((ICjNode) parent).indexOf((ICjGraph) child);
            }
            case Edge -> {
                assert child instanceof ICjGraph;
                return ((ICjEdge) parent).indexOf((ICjGraph) child);
            }
            default -> throw new AssertionError();
        }

    }

    /**
     * @return an id which resolves into the same URI, but abbreviated using the document {@code @context}.
     */
    default String abbreviatedUri() {
        String uri = uri();
        Map<String, String> ctx = documentContext();
        return CjUris.abbreviateUri(ctx, uri);
    }

    default ICjDocument document() {
        ICjElement parent = parent();
        if (parent instanceof ICjDocument) {
            return (ICjDocument) parent;
        } else if (parent instanceof ICjCoreElement coreElement) {
            return coreElement.document();
        } else {
            throw new AssertionError("Parent is " + parent.getClass().getSimpleName());
        }
    }

    /**
     * @return the document-level {@code @context} namespace map, or null if not set.
     */
    default @Nullable Map<String, String> documentContext() {
        return document().context();
    }

    /**
     * @return Either an {@link ICjDocument} or another {@link ICjCoreElement}.
     */
    @NonNull ICjElement parent();

    default Iterator<ICjElement> parentIterator() {
        return new ParentIterator(this);
    }

    /**
     * Walk tree up until we find an id, create position-based path from there. E.g. 'foo-g3-n4-g2-e77' (in an element
     * with id 'foo', graphs[3], in that graph node nodes[4], in that node graph graphs[2] and in that graph the edge
     * edges[77]).
     *
     * @return an id which will likely change if the document gets or looses elements
     */
    default @NonNull String unstableId() {
        String thisId = id();
        if (thisId != null) return thisId;

        Iterator<ICjElement> it = parentIterator();
        StringBuilder sb = new StringBuilder();
        // starts with the id-carrying element or the document itself
        List<ICjElement> parents = new ArrayList<>();
        while (it.hasNext()) {
            ICjElement parent = it.next();
            parents.add(parent);
            if (parent instanceof ICjHasId hasId) {
                String id = hasId.id();
                if (id != null) {
                    sb.append(id);
                    break;
                }
            } else {
                throw new AssertionError();
            }
        }
        // next, go from first parent down, always looking for position of next parent
        for (int i = 0; i < parents.size() - 1; i++) {
            ICjElement parent = parents.get(i);
            ICjElement child = parents.get(i + 1);
            int pos = findPositionOfChildInParent(parent, child);
            assert pos >= 0;
            sb.append('-');
            sb.append(pos);
        }
        return sb.toString();
    }

    /**
     * @return this element's URI, derived from this element's ID and the document {@code @context}.
     * It may be a blank node uri '_:' + {@link #unstableId()}.
     */
    @NonNull
    default String uri() {
        String id = id();
        if (id == null) {
            // we must create a blank node pseudo URI
            return BLANK_NODE_PSEUDO_SCHEME + unstableId();
        }

        return CjUris.expandId(documentContext(), id);
    }

}
