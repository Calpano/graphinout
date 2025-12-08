package com.graphinout.foundation.pure.annotations.license;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can annotate classes, methods, packages (package-info.java can contain annotations).
 *
 * Package annotations are inherited on to sub-packages. So annotating package-info in the root of a project annotates
 * the whole project.
 *
 * IMPROVE It would make a somehow cleaner process by using RetentionPolicy SOURCE, then write an AnnotationProcessor to
 * extract some XML file to be put in /META-INF, where another process collects them.
 *
 * @author xamde
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE,
		// TODO parse license annotations also for fields
		ElementType.FIELD })
@LicenseAnnotation(id = "Apache2_0", label = "Apache 2.0 License")
public @interface LicenseApache {

	/**
	 * @return complete list of contributors
	 */
	String contributors() default "";

	/**
	 * @return one line of copyright info
	 */
	String copyright() default "";

	/**
	 * All code annotated with this annotation is considered to be modified.
	 *
	 * @return true if code was modified (even if just the package was changed)
	 */
	boolean modified() default true;

	/**
	 * Resource name of NOTICE file, if any.
	 *
	 * Syntax convention: "NOTICE.(some unique name).txt", e.g. "NOTICE.apache-commons-lang.txt"
	 *
	 * @return
	 */
	String notice() default "";

	/**
	 * @return the short project name
	 */
	String project() default "";

}
