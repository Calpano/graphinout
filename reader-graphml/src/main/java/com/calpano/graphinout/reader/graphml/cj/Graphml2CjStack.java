package com.calpano.graphinout.reader.graphml.cj;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjDocumentMutable;
import com.calpano.graphinout.base.cj.element.ICjElement;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("UnusedReturnValue")
public class Graphml2CjStack {

    private ICjElement root;
    /** top of stack */
    private ICjElement current;

    @Nullable
    public ICjElement peek() {
        return current;
    }

    public ICjElement peek_() {
        return Objects.requireNonNull(current);
    }

    @Nullable
    public ICjElement pop() {
        ICjElement element = current;
        current = current.parent();
        return element;
    }

    public void pop(CjType expectedCjType) {
        ICjElement element = pop();
        assert element != null;
        assert element.cjType() == expectedCjType : "Expected " + expectedCjType + " but got " + element.cjType();
    }

    public ICjElement push(ICjElement cjElement) {
        assert current != null;
        current = cjElement;
        return cjElement;
    }

    public ICjDocumentMutable pushRoot() {
        root = new CjDocumentElement();
        current = root;
        return root.asDocument();
    }

    public ICjDocument root_() {
        return Objects.requireNonNull(root).asDocument();
    }

}
