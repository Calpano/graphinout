package com.graphinout.foundation.pure.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * This annotation exists only for clarity of documentation.
 * <p>
 * Methods tagged with this annotation change the state of the JVM, by using more memory. But they do not modify the underlying instance.
 * <p>
 * In REST, this maps best to POST.
 * <p>
 * See also: {@link ReadOperation}, @{@link ModificationOperation}
 *
 * @author xamde
 *
 */
@Target({ METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface CreateOperation {
	// annotation
}
