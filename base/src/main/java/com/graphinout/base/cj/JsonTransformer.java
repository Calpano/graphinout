package com.graphinout.base.cj;

import com.graphinout.foundation.json.value.IJsonArrayMutable;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonValue;

import java.util.ArrayList;
import java.util.List;

public class JsonTransformer {

    public interface IJsonTransformHandler {

        void transformArrayPost(List<Object> steps, IJsonArrayMutable arrayValue);

        void transformArrayPre(List<Object> steps, IJsonArrayMutable arrayValue);

        void transformObjectPost(List<Object> steps, IJsonObjectMutable o);

        void transformObjectPre(List<Object> steps, IJsonObjectMutable o);

    }

    private final IJsonTransformHandler handler;

    public JsonTransformer(IJsonTransformHandler handler) {this.handler = handler;}

    /**
     * @param jsonValue must have mutable containers
     */
    public void traverse(IJsonValue jsonValue) {
        traverse(List.of(), jsonValue);
    }

    private static List<Object> nextSteps(List<Object> steps, Object step) {
        List<Object> nextSteps = new ArrayList<>(steps);
        nextSteps.add(step);
        return nextSteps;
    }

    /**
     * Traverses the given jsonValue
     *
     * @param steps     the current location. Initially empty. Ordered from root to leaf.
     * @param jsonValue to traverse
     */
    private void traverse(List<Object> steps, IJsonValue jsonValue) {
        if (jsonValue.isPrimitive()) return;
        else if (jsonValue.isObject()) {
            IJsonObjectMutable o = (IJsonObjectMutable) jsonValue;
            handler.transformObjectPre(steps, o);
            o.forEach((k, v) -> traverse(nextSteps(steps,k), v));
            handler.transformObjectPost(steps, o);
        } else if (jsonValue.isArray()) {
            IJsonArrayMutable a = (IJsonArrayMutable) jsonValue;
            handler.transformArrayPre(steps, a);
            a.forEach((v, i) -> traverse(nextSteps(steps,i), v));
            handler.transformArrayPost(steps, a);
        } else throw new IllegalArgumentException("Not a known JSON type: " + jsonValue.getClass());
    }

}
