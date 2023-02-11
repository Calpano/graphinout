package com.calpano.graphinout.base.gio;

/**
 * Data types for {@link GioData} values, define in corresponding {@link GioKey}.
 */
public enum GioDataType {

    typeBoolean("boolean", boolean.class), //
    typeInt("int", int.class), //
    typeLong("long", long.class), //
    typeFloat("float", float.class), //
    typeDouble("double", double.class), //
    typeString("string", String.class);

    public final Class<?> clazz;
    public final String graphmlName;

    GioDataType(String graphmlName, Class<?> clazz) {
        this.graphmlName = graphmlName;
        this.clazz = clazz;
    }


}
