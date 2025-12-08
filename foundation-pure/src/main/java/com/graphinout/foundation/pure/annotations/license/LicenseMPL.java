package com.graphinout.foundation.pure.annotations.license;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * License: Mozilla Public License
 * <p>
 * Obligations: Code files licensed under the MPL must remain under the MPL and freely available in source form ... all
 * or none of the code in a given source file falls under the MPL.
 * <p>
 * You must include a copy of this License with every copy of the Source Code
 * <p>
 * You must cause all Covered Code to which You contribute to contain a file documenting the changes You made to create
 * that Covered Code and the date of any change<a href=".
 ">* <p>
 * See https:<a href="//www.moz</a>illa.org/MP<a href="L/1.0">ht</a>tps://www.mozilla.">...</a>org/MPL/1.1/ <a href="https://www.mozilla.org/MPL/2.0">...</a>
 * <p>
 * Can annotate classes, methods, packages (package-info.java can contain annotations).
 * <p>
 * Package annotations are inherited on to sub-packages. So annotating package-info in the root of a project annotates
 * the whole project.
 * <p>
 * IMPROVE It would make a somehow cleaner process by using RetentionPolicy SOURCE, then write an AnnotationProcessor to
 * extract some XML file to be put in /META-INF, where another process collects them.
 *
 * @author xamde
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE,
		// TODO parse license annotations also for fields
		ElementType.FIELD })
@LicenseAnnotation(id = "MPL", label = "Mozilla Public License")
public @interface LicenseMPL {

	/**
	 * Known license versions are 1.0, 1.1 and 2.0
	 *
	 * @return
	 */
	String licenseVersion();

	/**
	 * All code annotated with this annotation is considered to be modified.
	 *
	 * @return true if code was modified (even if just the package was changed)
	 */
	boolean modified() default true;

}
