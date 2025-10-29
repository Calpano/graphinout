package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.path.IJsonArrayNavigationStep;
import com.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.json.path.IJsonObjectNavigationStep;
import com.graphinout.foundation.json.value.IJsonArrayMutable;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * <h2>Rules</h2>
 * object cannot append; array cannot get property; primitive can append -> becomes array; primitive can get property ->
 * becomes object with <code>{ "value" : prev-value }</code>;
 * <p>
 * .API [ propertyKeys ]* (append|addProperty(p)) value
 */
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

    /**
     * Create a new nested JSON container structure with the given value
     *
     * @param path a chain of JSON property keys, intermediate objects are created as needed.
     * @param value         to set at propertySteps(last)
     */
    public static IJsonValue create(IJsonFactory factory, List<IJsonContainerNavigationStep> path, IJsonValue value) {
        if (path.isEmpty()) return value;
        // create intermediate objects & arrays
        IJsonContainerNavigationStep firstStep = path.getFirst();
        List<IJsonContainerNavigationStep> lastSteps = path.subList(1,path.size());
        return switch (firstStep.containerType()) {
            case Object -> {
                IJsonObjectMutable o = factory.createObjectMutable();
                IJsonObjectNavigationStep oStep = (IJsonObjectNavigationStep) firstStep;
                IJsonValue subValue = create(factory, lastSteps, value);
                o.addProperty(oStep.propertyKey(), subValue);
                yield o;
            }
            case Array -> {
                IJsonArrayMutable a = factory.createArrayMutable();
                IJsonArrayNavigationStep aStep = (IJsonArrayNavigationStep) firstStep;
                if (aStep.index() != 0) {
                    throw new IllegalArgumentException("Path mandates an array index " + aStep.index() + " but array is empty");
                }
                IJsonValue subValue = create(factory, lastSteps, value);
                a.add(subValue);
                yield a;
            }
        };
    }

    /**
     *
     * @param factory for creating new values
     * @param current    to which to append
     * @param path    e.g. 'foo'/'bar'/[2]/'baz', can be empty
     * @param value   e.g. 123
     * @return root replaced with the merge of (1) root and (2) value at the given path at root
     */
    public static IJsonValue merge(IJsonFactory factory, IJsonValue current, List<IJsonContainerNavigationStep> path, IJsonValue value) throws IllegalStateException {
        if (path.isEmpty()) { // need to merge root and value
            switch (current.jsonType().valueType()) {
                case Primitive -> {
                    // merge into a new array
                    IJsonArrayMutable a = factory.createArrayMutable();
                    a.add(current);
                    a.add(value);
                    return a;
                }
                case Array -> {
                    IJsonArrayMutable a = factory.asArrayMutable(current.asArray());
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
                    switch (current.jsonType().valueType()) {
                        case Primitive -> // merge 'foo' into a primitive => throw
                                throw new IllegalStateException("Cannot merge a propertyKey into a primitive");
                        case Object -> { // merge 'foo' into an object
                            IJsonObjectMutable rootAsMutableObject = factory.asObjectMutable(current.asObject());
                            if (rootAsMutableObject.hasProperty(objectStep.propertyKey())) {
                                merge(factory, current.asObject().get(objectStep.propertyKey()), remainingPath, value);
                                return current;
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
                    switch (current.jsonType().valueType()) {
                        case Primitive -> { // merge [0] into a primitive
                            IJsonArrayMutable a = factory.createArrayMutable();
                            a.add(current);
                            a.add(create(factory, remainingPath, value));
                            return a;
                        }
                        case Object -> { // merge [0] into object? throw
                            throw new IllegalStateException("Cannot merge an index into an object");
                        }
                        case Array -> { // merge [0] into array
                            IJsonArrayMutable a = factory.asArrayMutable(current.asArray());
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

    /**
     *
     * @param value
     * @param propertyKey
     * @return
     * @throws IllegalStateException if not a {@link IJsonObject}
     */
    public static IJsonValue removeProperty(
            IJsonValue value, String propertyKey) throws IllegalStateException {
        if(!value.isObject())
            throw new IllegalStateException();
        IJsonObject object = value.asObject();
        if(object instanceof IJsonObjectMutable objectMutable) {
            objectMutable.removeProperty(propertyKey);
            return object;
        } else {
            // create a copy without that property
            IJsonObjectMutable newObject = value.factory().createObjectMutable();
            for(String key : object.keys()) {
                if(!key.equals(propertyKey)) {
                    newObject.addProperty(key, object.get(key));
                }
            }
            return newObject;
        }
    }

}
