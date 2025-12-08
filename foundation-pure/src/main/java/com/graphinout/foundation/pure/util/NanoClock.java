package com.graphinout.foundation.pure.util;

import com.graphinout.foundation.pure.annotations.NotThreadSafe;
import org.jspecify.annotations.NonNull;

/**
 * A simple named clock for measuring time. Nanosecond precision.
 *
 * TODO move to xydra code or similar, rename into neutral name. In JS, there is only milliseconds.
 *
 * @author xamde
 *
 */

@NotThreadSafe
public class NanoClock {

	public static void main(final String[] args) {
		final NanoClock c = new NanoClock();
		c.start();
		c.stop("a");
		c.start();
		c.stop("b");
		c.start();
		c.stop("c");
		c.start();
		c.stop("d");
		System.out.println(c.getStats());
	}

	/**
	 * @return now in nanos
	 */
	private static long now() {
		return System.nanoTime();
	}

	private long firstStart = -1;

	private long lastStartTimeMillisUTC;

	/** -1 = not running */
	private long start = -1;

	private StringBuffer stats = new StringBuffer();

	public synchronized NanoClock append(@NonNull final String s) {
		this.stats.append(s);
		return this;
	}

	/**
	 * @return milliseconds elapsed since first start of this clock. -1 = clock was not started ever.
	 */
	public synchronized long getDurationSinceStart() {
		return (long) ((now() - this.firstStart) / 1000000d);
	}

	public long getLastStartTimeMillisUTC() {
		return this.lastStartTimeMillisUTC;
	}

	/**
	 * @return an HTML formatted string, only useful if clock has been started and stopped several times
	 */
	public synchronized String getStats() {
		return this.stats.toString() + " total=" + getDurationSinceStart() + "ms";
	}

	/**
	 * Turns the clock into a brand-new never-started clock
	 */
	public synchronized void reset() {
		this.start = -1;
		this.firstStart = -1;
		this.stats = new StringBuffer();
	}

	/**
	 * @return itself to allow elegant fluent code like this <code>Clock c = new Clock().start();</code>
	 */
	public synchronized NanoClock start() {
		this.start = now();
		this.lastStartTimeMillisUTC = System.currentTimeMillis();
		if (this.firstStart == -1) {
			this.firstStart = this.start;
		}
		return this;
	}

	/**
	 * @param name for the statistics @NeverNull
	 * @return this instance for API chaining
	 */
	public synchronized NanoClock stop(@NonNull final String name) {
		stopAndGetDuration(name);
		return this;
	}

	/**
	 * @param name for last clock entry @NeverNull
	 * @return duration since last start in milliseconds
	 */
	public synchronized long stopAndGetDuration(@NonNull final String name) {
		if (this.start == -1) {
			throw new IllegalStateException("Cannot stop a clock that was never started.");
		}
		final long stop = now();
		final double durationInMs = (stop - this.start) / 1000000d;
		this.stats.append(name).append("=").append(durationInMs).append("ms <br />\n");
		this.start = -1;
		return (long) durationInMs;
	}

	/**
	 * Stops the clock with the given name for the clock entry and immediately restarts it.
	 *
	 * @param name @NeverNull
	 */
	public synchronized void stopAndStart(@NonNull final String name) {
		stop(name);
		start();
	}

	/**
	 * @param name for last clock entry @NeverNull
	 * @return duration since last start in milliseconds
	 */
	public synchronized long stopStartAndGetLastDuration(@NonNull final String name) {
		final long d = stopAndGetDuration(name);
		start();
		return d;
	}

	public boolean wasStartedEver() {
		return this.firstStart >= 0;
	}

}
