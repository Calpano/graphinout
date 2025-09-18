package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Set;

import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;

/** Used for parsing XML as GraphML */
public class Cj2GraphmlStack {

    /** we use a LinkedList so we can peek deeper */
    private final LinkedList<Cj2GraphmlContext> elementStack = new LinkedList<>();

    public boolean isEmpty() {
        return elementStack.isEmpty();
    }

    public @Nullable Cj2GraphmlContext peekNullable() {
        return elementStack.peek();
    }

    public Cj2GraphmlContext peek_(CjType... expectedTypes) {
        Cj2GraphmlContext context = elementStack.peek();
        assert context != null;
        assert expectedTypes.length == 0 || Set.of(expectedTypes).contains(context.cjType) : "Expected element '" + Set.of(expectedTypes) + "' but got '" + context.cjType + "'";
        return context;
    }

    public Cj2GraphmlContext pop(CjType expectedType) {
        assert !elementStack.isEmpty() : "Element stack is empty at pop";
        Cj2GraphmlContext context = elementStack.pop();
        assert context.cjType.equals(expectedType) : "Expected element '" + expectedType + "' but got '" + context.cjType + "'";
        return context;
    }

    public Cj2GraphmlContext push(CjType cjType, GraphmlElementBuilder<?> builder) {
        Cj2GraphmlContext context = new Cj2GraphmlContext(peekNullable(), cjType, builder);
        elementStack.push(context);
        return context;
    }

    public @Nullable Cj2GraphmlContext root() {
        return elementStack.getFirst();
    }

    @Nullable
    GraphmlGraphBuilder findParentGraphElement() {
        // dig in stack to find the parent Graph element
        for (int i = elementStack.size() - 1; i >= 0; i--) {
            Cj2GraphmlContext context = elementStack.get(i);
            if (context.cjType.equals(GRAPH)) {
                return context.graphBuilder();
            }
        }
        return null;
    }

}
