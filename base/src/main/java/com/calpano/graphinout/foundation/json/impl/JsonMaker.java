package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.path.IJsonArrayNavigationStep;
import com.calpano.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.calpano.graphinout.foundation.json.path.IJsonObjectNavigationStep;
import com.calpano.graphinout.foundation.json.value.IJsonArrayMutable;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObjectMutable;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class JsonMaker {

    /**
     *
     * @param factory for creating new values
     * @param root    to which to append
     * @param path    e.g. 'foo'/'bar'/[2]/'baz', can be empty
     * @param value   e.g. 123
     * @return the root value replaced with a value that has with path=value added
     * @throws IllegalArgumentException if existing data and new path cannot be fit together
     */
    public static IJsonValue append(IJsonFactory factory, @Nullable IJsonValue root, List<IJsonContainerNavigationStep> path, IJsonValue value) throws IllegalArgumentException {
        if (root == null) {
            return create(factory, path, value);
        } else {
            return merge(factory, root, path, value);
        }
    }

    public static IJsonValue create(IJsonFactory factory, List<IJsonContainerNavigationStep> path, IJsonValue value) {
        if (path.isEmpty()) return value;
        // create intermediate objects & arrays
        IJsonContainerNavigationStep lastStep = path.getLast();
        return switch (lastStep.containerType()) {
            case Object -> {
                IJsonObjectMutable o = factory.createObjectMutable();
                IJsonObjectNavigationStep oStep = (IJsonObjectNavigationStep) lastStep;
                o.addProperty(oStep.propertyKey(), value);
                yield o;
            }
            case Array -> {
                IJsonArrayMutable a = factory.createArrayMutable();
                IJsonArrayNavigationStep aStep = (IJsonArrayNavigationStep) lastStep;
                if (aStep.index() != 0) {
                    throw new IllegalArgumentException("Path mandates an array index " + aStep.index() + " but array is empty");
                }
                a.add(value);
                yield a;
            }
        };
    }

    /**
     *
     * @param factory for creating new values
     * @param root    to which to append
     * @param path    e.g. 'foo'/'bar'/[2]/'baz', can be empty
     * @param value   e.g. 123
     * @return root replaced with the merge of (1) root and (2) value at the given path at root
     */
    public static IJsonValue merge(IJsonFactory factory, IJsonValue root, List<IJsonContainerNavigationStep> path, IJsonValue value) {
        if (path.isEmpty()) { // need to merge root and value
            switch (root.jsonType().valueType()) {
                case Primitive -> {
                    // merge into a new array
                    IJsonArrayMutable a = factory.createArrayMutable();
                    a.add(root);
                    a.add(value);
                    return a;
                }
                case Array -> {
                    IJsonArrayMutable a = factory.asArrayMutable(root.asArray());
                    a.add(value);
                    return a;
                }
                case Object -> throw new IllegalStateException("Cannot merge a value into an object");
            }
        } else {
            // merge root and step 0
            IJsonContainerNavigationStep firstStep = path.getFirst();
            List<IJsonContainerNavigationStep> remainingPath = path.subList(1, path.size());
            switch (firstStep.containerType()) {
                case Object -> {
                    IJsonObjectNavigationStep objectStep = (IJsonObjectNavigationStep) firstStep;
                    switch (root.jsonType().valueType()) {
                        case Primitive -> // merge 'foo' into a primitive => throw
                                throw new IllegalStateException("Cannot merge a propertyKey into a primitive");
                        case Object -> { // merge 'foo' into an object
                            IJsonObjectMutable rootAsMutableObject = factory.asObjectMutable(root.asObject());
                            if (rootAsMutableObject.hasProperty(objectStep.propertyKey())) {
                                return merge(factory, root.asObject().get(objectStep.propertyKey()), remainingPath, value);
                            } else {
                                // create it
                                IJsonValue subValue = create(factory, remainingPath, value);
                                rootAsMutableObject.addProperty(objectStep.propertyKey(), subValue);
                                return rootAsMutableObject;
                            }
                        }
                        case Array -> { // merge 'foo' into an array => throw
                            throw new IllegalStateException("Cannot merge a propertyKey into an array");
                        }
                    }
                }
                case Array -> {
                    IJsonArrayNavigationStep arrayStep = (IJsonArrayNavigationStep) firstStep;
                    switch (root.jsonType().valueType()) {
                        case Primitive -> { // merge [0] into a primitive
                            IJsonArrayMutable a = factory.createArrayMutable();
                            a.add(root);
                            a.add(create(factory, remainingPath, value));
                            return a;
                        }
                        case Object -> { // merge [0] into object? throw
                            throw new IllegalStateException("Cannot merge an index into an object");
                        }
                        case Array -> { // merge [0] into array
                            IJsonArrayMutable a = factory.asArrayMutable(root.asArray());
                            a.add(create(factory, remainingPath, value));
                            return a;
                        }
                    }
                }
                default -> throw new AssertionError("Unexpected value: " + firstStep.containerType());
            }
        }
        throw new AssertionError("unreachable");
    }

}
