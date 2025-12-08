package com.graphinout.reader.gml;

import com.graphinout.foundation.pure.json.document.IJsonPrimitive;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2JavaJsonWriter;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * GML data allows keys to occur multiple times.
 */
public class GmlData {

    /** linked hashmap to make debugging easier */
    private final Map<String, Object> map = new LinkedHashMap<>();

    public GmlData add(String key, Object value) {
        assert isJavaJsonPrimitive(value) || value instanceof GmlData;
        map.compute(key, (k, v) -> {
            if (v == null) {
                // no value yet, just store it
                return value;
            } else {
                // auto-convert to list
                //noinspection rawtypes
                if (v instanceof List list) {
                    //noinspection unchecked
                    list.add(value);
                    return list;
                } else {
                    List<Object> list = new ArrayList<>();
                    list.add(v);
                    list.add(value);
                    return list;
                }
            }
        });
        return this;
    }

    public void dump() {
        System.out.println("== Dump");
        dump("", map);
    }

    public void forEach(BiConsumer<String, Object> key_value) {
        map.forEach(key_value);
    }

    public void forEachChild(String key, Consumer<GmlData> childConsumer) {
        Object value = map.get(key);
        switch (value) {
            case null -> {
            }
            case GmlData gmlData -> childConsumer.accept(gmlData);
            case List list -> {
                for (Object o : list) {
                    if (o instanceof GmlData gmlData) {
                        childConsumer.accept(gmlData);
                    } else {
                        throw new IllegalStateException("List contained non-GMLData");
                    }
                }
            }
            default -> throw new IllegalStateException("Unexpected value type: " + value.getClass());
        }
    }

    public void forEachExcept(BiConsumer<String, Object> key_value, String... except) {
        Set<String> set = Set.of(except);
        forEach((k, v) -> {
            if (set.contains(k)) return;
            key_value.accept(k, v);
        });
    }

    public @Nullable String get(String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        } else {
            return value.toString();
        }
    }

    public static boolean isJavaJson(Object value) {
        return isJavaJsonPrimitive(value) || isJavaJsonList(value) || isJavaJsonMap(value);
    }

    public static boolean isJavaJsonList(Object value) {
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (!isJavaJson(o)) ;
            }
        }
        return false;
    }

    public static boolean isJavaJsonMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (Object o : map.values()) {
                if (!isJavaJson(o)) return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isJavaJsonPrimitive(Object value) {
        return value == null //
                || value instanceof Boolean //
                || value instanceof Number //
                || value instanceof String;
    }

    /**
     * Empty is JSON null.
     *
     * @param w
     */
    public void toJson(JsonWriter w) {
        if (map.isEmpty()) {
            w.onNull();
            return;
        }

        w.objectStart();
        map.forEach((key, value) -> {
            w.onKey(key);
            // key may be: GmlData -> JSON object, List -> JSON array, other -> JSON primitive
            if (value instanceof List<?> list) {
                w.arrayStart();
                for (Object o : list) {
                    toJson(o, w);
                }
                w.arrayEnd();
            } else {
                toJson(value, w);
            }
        });
        w.objectEnd();
    }

    public void toJson(Object o, JsonWriter w) {
        if (o instanceof GmlData gmlData) {
            gmlData.toJson(w);
        } else if (o instanceof IJsonPrimitive primitive) {
            primitive.fire(w);
        } else if (isJavaJsonPrimitive(o)) {
            IJsonValue jsonValue = GmlDocs.toJsonValue(o);
            jsonValue.fire(w);
        } else {
            throw new IllegalStateException("Unexpected value type: " + o.getClass());
        }
    }

    public IJsonValue toJsonValue() {
        Json2JavaJsonWriter writer = new Json2JavaJsonWriter();
        toJson(writer);
        return writer.jsonValue();
    }

    private void dump(String indent, Map<String, Object> map) {
        map.forEach((key, value) -> {
            if (value instanceof GmlData gmlData) {
                System.out.println(indent + key + " [");
                gmlData.dump(indent + "  ", gmlData.map);
                System.out.println(indent + "]");
            } else if (value instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof GmlData gmlData) {
                        System.out.println(indent + key + " [");
                        gmlData.dump(indent + "  ", gmlData.map);
                        System.out.println(indent + "]");
                    } else {
                        System.out.println(indent + key + " " + o);
                    }
                }
            } else {
                System.out.println(indent + key + " " + value);
            }
        });
    }


}
