package com.graphinout.foundation.json.value.java;

import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonPrimitive;

public class JavaJsonPrimitive implements IJsonPrimitive {

    public static final IJsonPrimitive NULL = new JavaJsonPrimitive(null);
    private final Object primitive;

    public JavaJsonPrimitive(Object primitive) {this.primitive = primitive;}

    public static IJsonPrimitive of(Object o) {
        return new JavaJsonPrimitive(o);
    }

    @Override
    public Object base() {
        return primitive;
    }

    @Override
    public IJsonFactory factory() {
        return JavaJsonFactory.INSTANCE;
    }

    public JsonType jsonType() {
        if (primitive == null) return JsonType.Null;

        if (JsonType.Boolean.javaClasses.contains(primitive.getClass())) {
            return JsonType.Boolean;
        }
        if (JsonType.String.javaClasses.contains(primitive.getClass())) {
            return JsonType.String;
        }
        if (JsonType.Number.javaClasses.contains(primitive.getClass())) {
            return JsonType.Number;
        }

        throw new AssertionError("Unknown node type: " + primitive.getClass());
    }

}
