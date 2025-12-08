package com.graphinout.foundation.pure.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * This annotation exists only for clarity of documentation.
 * <p>
 * Methods tagged with this annotation are side effect free read operations. In REST, methods like this are mapped to
 * GET.
 * <p>
 * Opposite annotation for writing/changing: {@link ModificationOperation}
 *
 * @author xamde
 *
 */
@Target({ METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ReadOperation {
	// annotation
}
