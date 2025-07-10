package com.calpano.graphinout.base.cj;

public interface CjEdgeType {

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

    CjEdgeTypeSource source();

    String type();

}
