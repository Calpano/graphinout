package com.graphinout.foundation.pure.functional;

@FunctionalInterface
public interface ByteArrayTransformerFunction {

	/**
	 * @param input
	 * @return
	 */
    byte[] transform(byte[] input) throws Exception;

}
