package com.graphinout.base.gio;

/**
 * Data types for {@link GioData} values, defined in corresponding {@link GioKey}.
 */
public enum GioDataType {

    typeBoolean("boolean", boolean.class), //
    typeInt("int", int.class), //
    typeLong("long", long.class), //
    typeFloat("float", float.class), //
    typeDouble("double", double.class), //
    typeString("string", String.class);

    public static GioDataType fromGraphmlName(String graphmlName) {
        for (GioDataType d : values()) {
            if (d.graphmlName.equals(graphmlName)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Could not interpret '" + graphmlName + "' as GioDataType");
    }

    public final Class<?> clazz;

    public String graphmlName() {
        return graphmlName;
    }

    public final String graphmlName;

    GioDataType(String graphmlName, Class<?> clazz) {
        this.graphmlName = graphmlName;
        this.clazz = clazz;
    }


}
