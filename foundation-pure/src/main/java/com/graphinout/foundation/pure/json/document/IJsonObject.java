package com.graphinout.foundation.pure.json.document;

import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.path.IJsonNavigationPath;
import com.graphinout.foundation.pure.json.path.IJsonObjectNavigationStep;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.nonNull;

/**
 * An immutable JSON object value: an ordered map of string keys to {@link IJsonValue}s.
 */
public interface IJsonObject extends IJsonContainer {


    @Override
    default void fire(JsonWriter jsonWriter) {
        jsonWriter.objectStart();
        keys().forEach(key -> {
            jsonWriter.onKey(key);
            IJsonValue value = get(key);
            if (value == null) jsonWriter.onNull();
            else value.fire(jsonWriter);
        });
        jsonWriter.objectEnd();
    }

    default void forEach(BiConsumer<String, IJsonValue> key_value) {
        keys().forEach(key -> key_value.accept(key, nonNull(get(key), factory()::createNull)));
    }

    default void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        forEach((key, value) -> //
        {
            IJsonNavigationPath path2 = prefix.withAppend(IJsonObjectNavigationStep.of(key));
            if (value.isPrimitive()) {
                // send out
                path_primitive.accept(path2, value.asPrimitive());
            } else {
                // RECURSE
                value.forEachLeaf(path2, path_primitive);
            }
        });
    }

    @Nullable IJsonValue get(String key);

    default <E extends Throwable, F extends Throwable> String getAsNonNullStringOrThrow(String key, Function<IJsonObject, E> ifNullExceptionSupplier, Function<IJsonValue, F> conversionErrorSupplier) throws E, F {
        IJsonValue value = get(key);
        if (value == null) {
            throw ifNullExceptionSupplier.apply(this);
        }
        return value.asStringOrThrow(conversionErrorSupplier);
    }

    default @Nullable Boolean getBoolean(String key) {
        return getBoolean(key, msg -> {});
    }

    default @Nullable Boolean getBoolean(String key, Consumer<String> errorHandler) throws IllegalStateException {
        IJsonValue v = get(key);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isBoolean()) {
            return v.asBoolean();
        } else {
            String msg = "['" + key + "'] is not a boolean but " + v.jsonType();
            errorHandler.accept(v.asString());
            throw new IllegalStateException(msg);
        }
    }

    default void getIfString(String key, Consumer<String> stringConsumer) {
        IJsonValue value = get(key);
        if (value != null && value.isString()) {
            stringConsumer.accept(value.asString());
        }
    }

    default void getMaybe(String propertyKey, Consumer<IJsonValue> valueConsumer) {
        IJsonValue value = get(propertyKey);
        if (value != null) {
            valueConsumer.accept(value);
        }
    }

    /**
     * @param propertyKey        to get
     * @param mapFun             return null to signal conversion failed silently
     * @param typedValueConsumer never gets nulls
     * @param <T>                type
     */
    default <T> void getMaybeAs(String propertyKey, Function<IJsonValue, T> mapFun, Consumer<T> typedValueConsumer) {
        getMaybe(propertyKey, jsonValue -> {
            T value = mapFun.apply(jsonValue);
            if (value != null) {
                typedValueConsumer.accept(value);
            }
        });
    }

    default <T, U> void getMaybeAs(String propertyKey, Function<IJsonValue, U> mapFun1, Function<U, T> mapFun2, Consumer<T> typedValueConsumer) {
        getMaybe(propertyKey, jsonValue -> {
            U value = mapFun1.apply(jsonValue);
            T value2 = mapFun2.apply(value);
            if (value != null) {
                typedValueConsumer.accept(value2);
            }
        });
    }

    default <F extends Throwable> @Nullable Boolean getNullOrBoolean(String key, Function<IJsonValue, F> conversionErrorSupplier) throws F {
        IJsonValue value = get(key);
        if (value == null) {
            return null;
        }
        return value.asBooleanOrThrow(conversionErrorSupplier);
    }

    default <F extends Throwable> @Nullable String getNullOrString(String key, Function<IJsonValue, F> conversionErrorSupplier) throws F {
        IJsonValue value = get(key);
        if (value == null) {
            return null;
        }
        return value.asStringOrThrow(conversionErrorSupplier);
    }

    default <F extends Throwable> @Nullable IJsonObject getObjectOrNull(String key, Function<IJsonValue, F> conversionErrorSupplier) throws F {
        IJsonValue value = get(key);
        if (value == null) {
            return null;
        }
        return value.asObjectOrThrow(conversionErrorSupplier);
    }

    default @Nullable String getString(String key) {
        return getString(key, msg -> {});
    }

    default @Nullable String getString(String key, Consumer<String> errorHandler) throws IllegalStateException {
        IJsonValue v = get(key);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isString()) {
            return v.asString();
        } else {
            String msg = "['" + key + "'] is not a string but " + v.jsonType();
            errorHandler.accept(v.asString());
            throw new IllegalStateException(msg);
        }
    }

    default @NonNull String getString_(String key) {
        return getString_(key, msg -> {throw new IllegalStateException(msg);});
    }

    /**
     *
     * @param key          to get
     * @param errorHandler to notify if the object contains nothing or something else than a String
     * @return the string
     * @throws IllegalStateException if the object contains nothing or something else than a String
     */
    default @NonNull String getString_(String key, Consumer<String> errorHandler) throws IllegalStateException {
        String s = getString(key, errorHandler);
        if (s == null) {
            String msg = "['" + key + "'] missing; expected a string.";
            errorHandler.accept(msg);
            throw new IllegalStateException(msg);
        }
        return s;
    }

    @NonNull
    default IJsonValue get_(String key) {
        IJsonValue v = get(key);
        if (v == null) {
            throw new IllegalStateException("Got null at '" + key + "'");
        }
        return v;
    }

    default boolean hasProperty(String propertyStep) {
        return keys().contains(propertyStep);
    }

    default void ifPresent(String key, Consumer<@NonNull IJsonValue> consumer) {
        IJsonValue value = get(key);
        if (value != null) {
            consumer.accept(value);
        }
    }

    default <T> void ifPresent(String key, Function<@NonNull IJsonValue, @Nullable T> mapFun, Consumer<@NonNull T> consumer) {
        IJsonValue value = get(key);
        if (value != null) {
            T t = mapFun.apply(value);
            if (t != null) {
                consumer.accept(t);
            }
        }
    }

    default <T, R> void ifPresent(String key, Function<@NonNull IJsonValue, @Nullable T> mapFun, Function<@NonNull T, @Nullable R> mapFun2, Consumer<@NonNull R> consumer) {
        IJsonValue value = get(key);
        if (value != null) {
            T t = mapFun.apply(value);
            if (t != null) {
                R r = mapFun2.apply(t);
                if (r != null) {
                    consumer.accept(r);
                }
            }
        }
    }

    default <T, R, S> void ifPresent(String key, //
                                     Function<@NonNull IJsonValue, @Nullable T> mapFun, //
                                     Function<@NonNull T, @Nullable R> mapFun2, //
                                     Function<@NonNull R, @Nullable S> mapFun3, //
                                     Consumer<@NonNull S> consumer) {
        IJsonValue value = get(key);
        if (value != null) {
            T t = mapFun.apply(value);
            if (t != null) {
                R r = mapFun2.apply(t);
                if (r != null) {
                    S s = mapFun3.apply(r);
                    if (s != null) {
                        consumer.accept(s);
                    }
                }
            }
        }
    }

    default boolean isArray() {return false;}

    default boolean isObject() {return true;}

    default JsonType jsonType() {
        return JsonType.Object;
    }

    Set<String> keys();

    /** Deep copy */
    default IJsonObjectMutable mutableCopy() {
        IJsonObjectMutable o = factory().createObjectMutable();
        forEach((k, v) -> {
            if (v.isObject()) {
                o.add(k, v.asObject().mutableCopy());
            } else if (v.isArray()) {
                o.add(k, v.asArray().mutableCopy());
            } else {
                o.add(k, v);
            }
        });
        return o;
    }

    default Stream<Map.Entry<String, IJsonValue>> properties() {
        return keys().stream().map(key -> new AbstractMap.SimpleImmutableEntry<>(key, get_(key)));
    }

    default int size() {
        return keys().size();
    }

    default Map<String, Object> toJaJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>(size());
        forEach((key, value) -> map.put(key, value.toJaJsonValue()));
        return map;
    }

    /** includes potentially null values */
    default Stream<IJsonValue> values() {
        return keys().stream().map(this::get_);
    }

    /**
     * Copy state into a Java Map
     * @return
     */
    default Map<String, IJsonValue> asMap() {
        Map<String, IJsonValue> map = new LinkedHashMap<>(size());
        forEach(map::put);
        return map;
    }

}
