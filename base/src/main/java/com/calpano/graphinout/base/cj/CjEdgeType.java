package com.calpano.graphinout.base.cj;

public interface CjEdgeType {

    CjEdgeTypeSource source();

    String type();

    static CjEdgeType of(CjEdgeTypeSource source, String type) {
        return new CjEdgeType() {
            @Override
            public CjEdgeTypeSource source() {
                return source;
            }

            @Override
            public String type() {
                return type;
            }
        };
    }

}
