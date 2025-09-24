package com.calpano.graphinout.base.graphml;

/**
 * Data types for {@link IGraphmlData} values, defined in corresponding {@link IGraphmlKey}.
 */
public enum GraphmlDataType {

    typeBoolean("boolean", boolean.class), //
    typeInt("int", int.class), //
    typeLong("long", long.class), //
    typeFloat("float", float.class), //
    typeDouble("double", double.class), //
    typeString("string", String.class);

    public final Class<?> clazz;
    public final String graphmlName;

    GraphmlDataType(String graphmlName, Class<?> clazz) {
        this.graphmlName = graphmlName;
        this.clazz = clazz;
    }

    public static GraphmlDataType fromGraphmlName(String graphmlName) {
        for (GraphmlDataType d : values()) {
            if (d.graphmlName.equals(graphmlName)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Could not interpret '" + graphmlName + "' as GioDataType");
    }

    public String graphmlName() {
        return graphmlName;
    }


}
