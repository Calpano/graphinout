package com.graphinout.reader.graphml.cj;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonPrimitive;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * GraphML 1.1 time-stamped data: {@code <data key="k" time="1">} (GraphML 1.1, issue #158).
 * <p>
 * GraphML 1.1 allows several {@code <data>} with the same key on one element, one per {@code time} (an
 * {@code xs:long}, default 0). In CJ, such a property holds a <em>time series</em>: an array of
 * {@code {"time": t, "value": v}} objects, one per {@code <data>}, in document order. An entry without {@code time}
 * stands for a {@code <data>} without the attribute, i.e. the schema default 0.
 * <pre>
 * &lt;data key="w" time="1"&gt;1&lt;/data&gt;&lt;data key="w" time="2"&gt;5&lt;/data&gt;
 *   ⇔  "w": [{"time": 1, "value": 1}, {"time": 2, "value": 5}]
 * </pre>
 */
public final class GraphmlTimeValues {

    /** The GraphML attribute on {@code <data>}. */
    public static final String ATTRIBUTE_TIME = "time";
    /** The GraphML attribute on {@code <key>} marking values that change over time. */
    public static final String ATTRIBUTE_DYNAMIC = "dynamic";
    /** Property of a time series entry holding the time. */
    public static final String TIME = "time";
    /** Property of a time series entry holding the value. */
    public static final String VALUE = "value";

    private static final Set<String> ENTRY_KEYS = Set.of(TIME, VALUE);

    /** One {@code <data>} of a time series: its value and its {@code time}, if given. */
    public record Entry(@Nullable IJsonValue time, IJsonPrimitive value) {}

    private GraphmlTimeValues() {}

    /**
     * @return true if {@code value} is a time series: a non-empty array of objects, each with a primitive
     * {@code value} and an optional primitive {@code time}, and nothing else
     */
    public static boolean isTimeSeries(@Nullable IJsonValue value) {
        if (value == null || !value.isArray()) return false;
        IJsonArray array = value.asArray();
        if (array.size() == 0) return false;
        for (int i = 0; i < array.size(); i++) {
            if (!isEntry(array.get(i))) return false;
        }
        return true;
    }

    private static boolean isEntry(IJsonValue v) {
        if (!v.isObject()) return false;
        IJsonObject o = v.asObject();
        if (!ENTRY_KEYS.containsAll(o.keys())) return false;
        IJsonValue value = o.get(VALUE);
        if (value == null || !value.isPrimitive()) return false;
        IJsonValue time = o.get(TIME);
        return time == null || time.isPrimitive();
    }

    /** The entries of a time series, in order. */
    public static List<Entry> entries(IJsonValue timeSeries) {
        assert isTimeSeries(timeSeries);
        List<Entry> entries = new ArrayList<>();
        timeSeries.asArray().forEach(v -> {
            IJsonObject o = v.asObject();
            entries.add(new Entry(o.get(TIME), o.get(VALUE).asPrimitive()));
        });
        return entries;
    }

    /**
     * The value to store after adding one {@code <data>} to a property.
     *
     * @param existing the property's current value, or null if not set yet
     * @param time     the {@code time} attribute of the {@code <data>}, or null if absent
     * @param value    the value of the {@code <data>}
     * @return a time series, or null if neither the new {@code <data>} nor the existing value is time-stamped (then the
     * caller keeps its usual behaviour)
     */
    public static @Nullable IJsonValue add(IJsonFactory factory, @Nullable IJsonValue existing, @Nullable String time,
            IJsonPrimitive value) {
        boolean existingIsSeries = isTimeSeries(existing);
        if (time == null && !existingIsSeries) return null;
        IJsonArrayMutable series = factory.createArrayMutable();
        if (existingIsSeries) {
            existing.asArray().forEach(series::add);
        } else if (existing != null) {
            // values read before the first time-stamped one have no time
            if (existing.isArray()) existing.asArray().forEach(v -> series.add(entry(factory, null, v)));
            else series.add(entry(factory, null, existing));
        }
        series.add(entry(factory, time == null ? null : timeValue(factory, time), value));
        return series;
    }

    private static IJsonObjectMutable entry(IJsonFactory factory, @Nullable IJsonValue time, IJsonValue value) {
        IJsonObjectMutable o = factory.createObjectMutable();
        if (time != null) o.addProperty(TIME, time);
        o.addProperty(VALUE, value);
        return o;
    }

    /** {@code time} is an {@code xs:long}; keep it as a number, or as the string as written if it is none. */
    private static IJsonValue timeValue(IJsonFactory factory, String time) {
        try {
            return factory.createLong(Long.parseLong(time.trim()));
        } catch (NumberFormatException e) {
            return factory.createString(time);
        }
    }

    /** The {@code time} attribute value to write for an entry, or null if it has none. */
    public static @Nullable String timeAttribute(Entry entry) {
        IJsonValue time = entry.time();
        if (time == null) return null;
        return time.isPrimitive() ? String.valueOf(time.asPrimitive().base()) : time.toJsonString();
    }
}
