package com.graphinout.foundation.pure.functional;

import com.graphinout.foundation.pure.annotations.GwtIncompatible;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@GwtIncompatible("File")
public class Caching {

	/**
	 * A computation for which the result can be stored in a cache file. If the cache file exists, it is re-loaded. If
	 * not, the computation is performed and the result stored.
	 *
	 * @param input computation input
	 * @param cacheFileFn The mapping from input to cache file
	 * @param computeFn
	 * @param loadFn
	 * @param saveFn
	 * @return loaded or computed output
	 */
	public static <I, O> O getCachedOrCompute(final I input, final Function<I, File> cacheFileFn,
			final Function<I, O> computeFn, final Function<File, O> loadFn, final BiConsumer<O, File> saveFn) {
		final File cacheFile = cacheFileFn.apply(input);
		if (cacheFile.exists()) {
			final O output = loadFn.apply(cacheFile);
			return output;
		} else {
			final O output = computeFn.apply(input);
			saveFn.accept(output, cacheFile);
			return output;
		}
	}

	public static <O> O getCachedOrCompute(final File cacheFile, final Supplier<O> computeFn,
			final Function<File, O> loadFn, final BiConsumer<O, File> saveFn) {
		if (cacheFile.exists()) {
			final O output = loadFn.apply(cacheFile);
			return output;
		} else {
			final O output = computeFn.get();
			saveFn.accept(output, cacheFile);
			return output;
		}
	}
}
