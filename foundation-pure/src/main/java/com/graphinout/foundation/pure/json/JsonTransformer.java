package com.graphinout.foundation.pure.json;

import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.bridge.Java9;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JsonTransformer {

    public interface IJsonTransformHandler {

        void transformArrayPost(List<Object> steps, IJsonArrayMutable arrayValue);

        void transformArrayPre(List<Object> steps, IJsonArrayMutable arrayValue);

        void transformObjectPost(List<Object> steps, IJsonObjectMutable o);

        void transformObjectPre(List<Object> steps, IJsonObjectMutable o);

    }
    private final IJsonTransformHandler handler;

    public JsonTransformer(IJsonTransformHandler handler) {this.handler = handler;}

    private static List<Object> nextSteps(List<Object> steps, Object step) {
        List<Object> nextSteps = new ArrayList<>(steps);
        nextSteps.add(step);
        return nextSteps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        JsonTransformer that = (JsonTransformer) obj;
        return Objects.equals(this.handler, that.handler);
    }

    public IJsonTransformHandler handler() {return handler;}

    @Override
    public int hashCode() {
        return Objects.hash(handler);
    }

    @Override
    public String toString() {
        return "JsonTransformer[" +
                "handler=" + handler + ']';
    }

    /**
     * @param jsonValue must have mutable containers
     */
    public void traverse(IJsonValue jsonValue) {
        traverse(Java9.List.of(), jsonValue);
    }

    /**
     * Traverses the given jsonValue
     *
     * @param steps     the current location. Initially empty. Ordered from root to leaf.
     * @param jsonValue to traverse
     */
    private void traverse(List<Object> steps, IJsonValue jsonValue) {
        if (jsonValue.isPrimitive()) {
            return;
        } else if (jsonValue.isObject()) {
            IJsonObjectMutable o = (IJsonObjectMutable) jsonValue;
            handler.transformObjectPre(steps, o);
            o.forEach((k, v) -> traverse(nextSteps(steps, k), v));
            handler.transformObjectPost(steps, o);
        } else if (jsonValue.isArray()) {
            IJsonArrayMutable a = (IJsonArrayMutable) jsonValue;
            handler.transformArrayPre(steps, a);
            a.forEach((v, i) -> traverse(nextSteps(steps, i), v));
            handler.transformArrayPost(steps, a);
        } else throw new IllegalArgumentException("Not a known JSON type: " + jsonValue.getClass());
    }


}
