package com.graphinout.foundation.pure.json.util;

import com.graphinout.foundation.pure.collections.jajson.JaJsonMapBuilder;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.path.IJsonArrayNavigationStep;
import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.pure.json.path.IJsonObjectNavigationStep;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Uses JSON like a flexible container, which shapes its type and structure as more elements are added.
 *
 * <h2>Rules</h2>
 * object cannot append; array cannot get property; primitive can append -> becomes array; primitive can get property ->
 * becomes object with <code>{ "value" : prev-value }</code>;
 * <p>
 * .API [ propertyKeys ]* (append|addProperty(p)) value
 */
@SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "PatternVariableCanBeUsed"})
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
     * @param path  a chain of JSON property keys, intermediate objects are created as needed.
     * @param value to set at propertySteps(last)
     */
    public static IJsonValue create(IJsonFactory factory, List<IJsonContainerNavigationStep> path, IJsonValue value) {
        if (path.isEmpty()) return value;
        // create intermediate objects & arrays
        IJsonContainerNavigationStep firstStep = path.get(0);
        List<IJsonContainerNavigationStep> lastSteps = path.subList(1, path.size());
        switch (firstStep.containerType()) {
            case Object: {
                IJsonObjectMutable o = factory.createObjectMutable();
                IJsonObjectNavigationStep oStep = (IJsonObjectNavigationStep) firstStep;
                IJsonValue subValue = create(factory, lastSteps, value);
                o.addProperty(oStep.propertyKey(), subValue);
                return o;
            }
            case Array: {
                IJsonArrayMutable a = factory.createArrayMutable();
                IJsonArrayNavigationStep aStep = (IJsonArrayNavigationStep) firstStep;
                if (aStep.index() != 0) {
                    throw new IllegalArgumentException("Path mandates an array index " + aStep.index() + " but array is empty");
                }
                IJsonValue subValue = create(factory, lastSteps, value);
                a.add(subValue);
                return a;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * <h2>Merge Semantics</h2>
     * Arrays are appended, i.e. array index is always ignored.
     *
     * @param factory for creating new values
     * @param current to which to append
     * @param path    e.g. 'foo'/'bar'/[2]/'baz', can be empty
     * @param value   e.g. 123
     * @return root replaced with the merge of (1) root and (2) value at the given path at root
     */
    public static IJsonValue merge(IJsonFactory factory, IJsonValue current, List<IJsonContainerNavigationStep> path, IJsonValue value) throws IllegalStateException {
        if (path.isEmpty()) { // need to merge root and value
            switch (current.jsonType().valueType()) {
                case Primitive: {
                    // merge into a new array
                    IJsonArrayMutable a = factory.createArrayMutable();
                    a.add(current);
                    a.add(value);
                    return a;
                }
                case Array: {
                    IJsonArrayMutable a = factory.asArrayMutable(current.asArray());
                    a.add(value);
                    return a;
                }
                case Object:
                    throw new IllegalStateException("Cannot merge a value '" + value + "' into an object '" + current + "'");
            }
        } else {
            // merge root and step 0
            IJsonContainerNavigationStep firstStep = path.get(0);
            List<IJsonContainerNavigationStep> remainingPath = path.subList(1, path.size());
            switch (firstStep.containerType()) {
                case Object:
                    IJsonObjectNavigationStep objectStep = (IJsonObjectNavigationStep) firstStep;
                    switch (current.jsonType().valueType()) {// merge 'foo' into a primitive => throw
                        case Primitive:
                            throw new IllegalStateException("Cannot merge a propertyKey into a primitive");
                        case Object:// merge 'foo' into an object
                            IJsonObjectMutable rootAsMutableObject = factory.asObjectMutable(current.asObject());
                            String propertyKey = objectStep.propertyKey();
                            if (rootAsMutableObject.hasProperty(propertyKey)) {
                                merge(factory, current.asObject().get(propertyKey), remainingPath, value);
                                return current;
                            } else {
                                // create it
                                IJsonValue subValue = create(factory, remainingPath, value);
                                rootAsMutableObject.addProperty(propertyKey, subValue);
                                return rootAsMutableObject;
                            }
                        case Array:// merge 'foo' into an array => throw
                            throw new IllegalStateException("Cannot merge a propertyKey into an array");
                    }
                case Array:
                    IJsonArrayNavigationStep arrayStep = (IJsonArrayNavigationStep) firstStep;
                    // index not used for merge, we simply append
                    int index = arrayStep.index();
                    switch (current.jsonType().valueType()) {
                        case Primitive: {
                            // merge [i] into a primitive => convert primitive to array, append all other values
                            IJsonArrayMutable a = factory.createArrayMutable();
                            a.add(current);
                            a.add(create(factory, remainingPath, value));
                            return a;
                        }
                        case Object: {
                            // merge [i] into object? throw
                            throw new IllegalStateException("Cannot merge an index ('" + index +
                                    "') into an object");
                        }
                        case Array: {
                            // merge [0] into array
                            IJsonArrayMutable rootAsMutableArray = factory.asArrayMutable(current.asArray());

                            rootAsMutableArray.add(create(factory, remainingPath, value));
                            return rootAsMutableArray;
                        }
                    }
                default:
                    throw new AssertionError("Unexpected value: " + firstStep.containerType());
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
    public static IJsonValue removeProperty(IJsonValue value, String propertyKey) throws IllegalStateException {
        if (!value.isObject()) throw new IllegalStateException();
        IJsonObject object = value.asObject();
        if (object instanceof IJsonObjectMutable) {
            IJsonObjectMutable objectMutable = (IJsonObjectMutable) object;
            objectMutable.removeProperty(propertyKey);
            return object;
        } else {
            // create a copy without that property
            IJsonObjectMutable newObject = value.factory().createObjectMutable();
            for (String key : object.keys()) {
                if (!key.equals(propertyKey)) {
                    newObject.addProperty(key, object.get(key));
                }
            }
            return newObject;
        }
    }

}
