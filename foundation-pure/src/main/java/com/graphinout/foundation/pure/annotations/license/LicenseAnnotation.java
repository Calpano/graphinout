package com.graphinout.foundation.pure.annotations.license;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-meta data to record the fact that an annotation contains license information about a package, class or method.
 * <p>
 * All license annotations must have the methods
 * <p>
 * String project(); boolean modified();
 *
 * @author xamde
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface LicenseAnnotation {

	/**
	 * @return a unique, short ID for the license.
	 */
	String id();

	/**
	 * @return human-readable name of license, includes version number
	 */
	String label();

}
