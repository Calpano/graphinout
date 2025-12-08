package com.graphinout.base.cj.document;

import com.graphinout.foundation.pure.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;

import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.mapOrDefault;
import static com.graphinout.foundation.pure.functional.Nullables.mapOrNull;

/**
 * Represents a CJ data value attached to elements in the CJ model, exposing access to the underlying JSON structure. It
 * is the bridge between GIO/CJ structures and the JSON value API used for properties and metadata.
 * <p>
 * This interface provides methods to access and manipulate the JSON data associated with a CJ element.
 * It allows for querying properties and navigating the JSON structure.
 */
public interface ICjData extends ICjElement {

    /**
     * {@inheritDoc}
     * <p>
     * CJ data elements do not have any sub-elements, so this method always returns an empty stream.
     */
    @Override
    default Stream<ICjElement> directChildren() {
        // CJ data has no sub-elements
        return Stream.empty();
    }

    /**
     * Returns the factory used to create JSON values.
     *
     * @return The JSON factory.
     */
    IJsonFactory factory();

    /**
     * Retrieves a JSON value from the underlying JSON structure using a navigation path.
     *
     * @param path The list of navigation steps to locate the desired JSON value.
     * @return The {@link IJsonValue} at the specified path, or {@code null} if not found.
     */
    default @Nullable IJsonValue get(List<IJsonContainerNavigationStep> path) {
        return mapOrNull(jsonValue(), j -> j.get(path));
    }

    /**
     * Retrieves a JSON value for a given property key.
     *
     * @param propertyKey The key of the property to retrieve.
     * @return The {@link IJsonValue} for the given property key, or {@code null} if not found.
     */
    default @Nullable IJsonValue getProperty(String propertyKey) {
        return get(IJsonContainerNavigationStep.pathOf(propertyKey));
    }

    /**
     * Checks if a JSON value exists at a specified navigation path.
     *
     * @param path The list of navigation steps to check for existence.
     * @return {@code true} if a value exists at the path, {@code false} otherwise.
     */
    default boolean has(List<IJsonContainerNavigationStep> path) {
        return mapOrDefault(jsonValue(), j -> j.has(path), false);
    }

    /**
     * Checks if a property with the given key exists.
     *
     * @param propertyKey The key of the property to check.
     * @return {@code true} if the property exists, {@code false} otherwise.
     */
    default boolean hasProperty(String propertyKey) {
        return has(IJsonContainerNavigationStep.pathOf(propertyKey));
    }

    /**
     * The current JSON content of this data element.
     *
     * @return The underlying {@link IJsonValue}, or {@code null} if there is no JSON content.
     */
    @Nullable
    IJsonValue jsonValue();

    /**
     * Returns the non-null JSON value. This is a convenience method for cases where the JSON value is known to exist.
     *
     * @return The non-null {@link IJsonValue}.
     * @throws AssertionError if the JSON value is {@code null}.
     */
    default IJsonValue jsonValue_() {
        IJsonValue value = jsonValue();
        assert value != null;
        return value;
    }

    /**
     * Converts the underlying JSON value to its Java JSON representation.
     *
     * @return The Java JSON object, or {@code null} if there is no JSON content.
     */
    default Object toJaJsonValue() {
        IJsonValue json = jsonValue();
        if (json == null) {
            return null;
        }
        return json.toJaJsonValue();
    }

}
