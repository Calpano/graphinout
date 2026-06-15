package com.graphinout.foundation.pure.collections;

/**
 * Generates unique string ids (e.g. for synthetic node or edge identifiers).
 */
public interface IdFactory {

    class CountingIdFactory implements IdFactory {

        private final String idPrefix;
        int count = 0;

        public CountingIdFactory(String idPrefix) {this.idPrefix = idPrefix;}

        @Override
        public String createId() {
            return idPrefix + count++;
        }

        /**
         * Allows to (re-)set the count to avoid collisions easier.
         *
         * @param count to set
         */
        public void setCount(int count) {
            assert count >= this.count;
            this.count = count;
        }

    }

    static CountingIdFactory createCounting(String idPrefix) {
        return new CountingIdFactory(idPrefix);
    }

    /**
     * create an id, unique within implementing class context
     */
    String createId();

}
