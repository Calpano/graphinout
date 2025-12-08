package com.graphinout.foundation.pure.value;

import java.math.BigDecimal;

public class BigDecimalRef {

    BigDecimal value;

    public BigDecimalRef(BigDecimal value) {
        this.value = value;
    }

    public void add(BigDecimal other) {
        this.value = this.value.add(other);
    }

    public BigDecimal get() {
        return value;
    }

    public void subtract(BigDecimal other) {
        this.value = this.value.subtract(other);
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
