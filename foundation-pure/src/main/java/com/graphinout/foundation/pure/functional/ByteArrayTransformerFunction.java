package com.graphinout.foundation.pure.functional;

/**
 * Transforms a byte array into another byte array (e.g. compression or encoding); may throw.
 */
@FunctionalInterface
public interface ByteArrayTransformerFunction {

	/**
	 * @param input
	 * @return
	 */
    byte[] transform(byte[] input) throws Exception;

}
