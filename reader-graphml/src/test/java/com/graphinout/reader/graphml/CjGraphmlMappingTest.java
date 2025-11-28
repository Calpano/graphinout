package com.graphinout.reader.graphml;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.foundation.json.value.java.JavaJsonFactory.INSTANCE;
import static com.graphinout.reader.graphml.cj.CjGraphmlMapping.toGraphmlType;
import static com.graphinout.reader.graphml.elements.GraphmlDataType.typeBoolean;
import static com.graphinout.reader.graphml.elements.GraphmlDataType.typeDouble;
import static com.graphinout.reader.graphml.elements.GraphmlDataType.typeFloat;
import static com.graphinout.reader.graphml.elements.GraphmlDataType.typeInt;

class CjGraphmlMappingTest {


    @Test
    void test() {
        assertThat(toGraphmlType(INSTANCE.createBoolean(true))).isEqualTo(typeBoolean);

        assertThat(toGraphmlType(INSTANCE.createInteger(12))).isEqualTo(typeInt);
        assertThat(toGraphmlType(INSTANCE.createLong(12))).isEqualTo(typeInt);
        assertThat(toGraphmlType(INSTANCE.createFloat(12))).isEqualTo(typeInt);
        assertThat(toGraphmlType(INSTANCE.createDouble(12))).isEqualTo(typeInt);
        assertThat(toGraphmlType(INSTANCE.createBigInteger(BigInteger.valueOf(12)))).isEqualTo(typeInt);
        assertThat(toGraphmlType(INSTANCE.createBigDecimal(BigDecimal.valueOf(12)))).isEqualTo(typeInt);
        assertThat(toGraphmlType(INSTANCE.createNumber(12))).isEqualTo(typeInt);
        // this is rounding at creation time to nearest float
        assertThat(toGraphmlType(INSTANCE.createFloat(12.34f))).isEqualTo(typeFloat);
        // does not fit in a float
        assertThat(toGraphmlType(INSTANCE.createDouble(12.34d))).isEqualTo(typeDouble);
        assertThat(toGraphmlType(INSTANCE.createBigDecimal(BigDecimal.valueOf(12.34)))).isEqualTo(typeDouble);
        assertThat(toGraphmlType(INSTANCE.createNumber(12.34))).isEqualTo(typeDouble);
    }

}
