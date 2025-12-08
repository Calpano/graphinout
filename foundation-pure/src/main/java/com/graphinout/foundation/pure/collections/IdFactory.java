package com.graphinout.foundation.pure.collections;

public interface IdFactory {

    static IdFactory createCounting(String idPrefix) {
        return new IdFactory() {

            int count = 0;

            @Override
            public String createId() {
                return idPrefix + count++;
            }
        };
    }

    /**
     * create an id, unique within implementing class context
     */
    String createId();

}
