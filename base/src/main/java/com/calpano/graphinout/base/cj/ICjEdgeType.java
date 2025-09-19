package com.calpano.graphinout.base.cj;

public interface ICjEdgeType {

    static ICjEdgeType of(CjEdgeTypeSource source, String type) {
        return new ICjEdgeType() {
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
