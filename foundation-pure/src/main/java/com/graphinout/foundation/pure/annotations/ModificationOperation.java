package com.graphinout.foundation.pure.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * This annotation exists only for clarity of documentation.
 * <p>
 * Methods tagged with this annotation change the state. In REST, they are mapped to PUT, POST or DELETE.
 * <p>
 * Opposite annotation for writing/changing: {@link ReadOperation}
 *
 * @author xamde
 *
 */
@Target({ METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ModificationOperation {
	// annotation
}
