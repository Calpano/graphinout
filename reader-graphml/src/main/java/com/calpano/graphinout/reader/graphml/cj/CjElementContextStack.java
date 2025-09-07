package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.element.impl.CjElement;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
public class CjElementContextStack {

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

    public void peek_push(Function<CjElement, CjElement> parentChildFun) {
        CjElement parent = peek_();
        CjElement child = parentChildFun.apply(parent);
        push(child);
    }

    @Nullable
    public CjElement pop() {
        current = current.parent;
        return current;
    }

    public CjElement push(CjElement cjElement) {
        assert current != null;
        current = cjElement;
        return cjElement;
    }

    public CjDocumentElement pushRoot() {
        root = new CjDocumentElement(null);
        current = root;
        return root.asDocument();
    }

    public CjDocumentElement root_() {
        return Objects.requireNonNull(root).asDocument();
    }

}
