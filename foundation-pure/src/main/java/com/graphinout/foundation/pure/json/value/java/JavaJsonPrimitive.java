package com.graphinout.foundation.pure.json.value.java;

import com.graphinout.foundation.pure.json.JsonType;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonPrimitive;
import org.jspecify.annotations.Nullable;

public class JavaJsonPrimitive implements IJsonPrimitive {

    public static final IJsonPrimitive NULL = new IJsonPrimitive() {
        @Override
        public Object base() {
            return null;
        }

        @Override
        public IJsonFactory factory() {
            return JavaJsonFactory.INSTANCE;
        }

        @Override
        public JsonType jsonType() {
            return JsonType.Null;
        }

        @Override
        public String toString() {
            return "json:NULL";
        }
    };
    private final @Nullable Object primitive;

    public JavaJsonPrimitive(@Nullable Object primitive) {this.primitive = primitive;}

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

    @Override
    public String toString() {
        return "" + primitive;
    }

}
