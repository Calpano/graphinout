package com.graphinout.foundation.pure.bridge;

import com.graphinout.foundation.pure.annotations.GwtIncompatible;

/**
 * There is little reflection possible in j2cl. Basically we have {@link java.lang.Class#getName()}.
 */
@SuppressWarnings("unused")
public class JavaPlatform {

    /** JVM version */
    private static class Vary extends JavaPlatform.Vary_j2cl {

        @Override
        @GwtIncompatible("Class.instanceOf")
        public <T extends S, S> T cast(java.lang.Class<T> clazz, S top) {
            return clazz.cast(top);
        }

    }

    /* JS version */
    private static class Vary_j2cl {

        public <T extends S, S> T cast(java.lang.Class<T> clazz, S value) {
            //noinspection unchecked
            return (T) value;
        }

        public <T extends S, S> boolean isInstanceExact(java.lang.Class<T> clazz, S value) {
            return value.getClass().getName().equals(clazz.getName());
        }

    }

    public static class Class {

        public static <T extends S, S> T cast(java.lang.Class<T> clazz, S value) {
            return VARY.cast(clazz, value);
        }

        /**
         * Is the given value exactly of the given class? Or, for a much better check, use classic {@code instanceof}.
         *
         * @param clazz
         * @param value
         * @param <T>
         * @param <S>
         * @return
         */
        public static <T extends S, S> boolean isInstanceExact(java.lang.Class<T> clazz, S value) {
            return value.getClass().getName().equals(clazz.getName());
        }

    }

    private static final JavaPlatform.Vary VARY = new JavaPlatform.Vary();


}
