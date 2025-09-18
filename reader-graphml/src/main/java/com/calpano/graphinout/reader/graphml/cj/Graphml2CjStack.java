package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.element.impl.CjElement;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("UnusedReturnValue")
public class Graphml2CjStack {

    private CjElement root;
    /** top of stack */
    private CjElement current;

    @Nullable
    public CjElement peek() {
        return current;
    }

    public CjElement peek_() {
        return Objects.requireNonNull(current);
    }

    @Nullable
    public CjElement pop() {
        CjElement element = current;
        current = current.parent;
        return element;
    }

    public void pop(CjType expectedCjType) {
        CjElement element = pop();
        assert element != null;
        assert element.cjType() == expectedCjType : "Expected "+expectedCjType+" but got "+element.cjType();
    }

    public CjElement push(CjElement cjElement) {
        assert current != null;
        current = cjElement;
        return cjElement;
    }

    public CjDocumentElement pushRoot() {
        root = new CjDocumentElement();
        current = root;
        return root.asDocument();
    }

    public CjDocumentElement root_() {
        return Objects.requireNonNull(root).asDocument();
    }

}
