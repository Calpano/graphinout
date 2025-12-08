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
 * About the license: <a href="https://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License">...</a>
 *
 * @author xamde
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE })
@LicenseAnnotation(id = "LGPL", label = "GNU Library General Public License")
public @interface LicenseLGPL {

	/**
	 * @return complete list of contributors
	 */
	String contributors() default "";

	/**
	 * @return one line of copyright info
	 */
	String copyright() default "";

	/**
     * From Wikipedia: The license was originally called the GNU Library General Public License and was first published
     * in 1991, and adopted the version number 2 for parity with GPL version 2.
     *
     * The LGPL was revised in minor ways in the 2.1 point release, published in 1999, when it was renamed the GNU
     * Lesser General Public License to reflect the FSF's position that not all libraries should use it.
     *
     * Version 3 of the LGPL was published in 2007 as a list of additional permissions applied to GPL version 3.
     *
     * See also <a href="https://www.gnu.org/licenses/lgpl-java.html">...</a>
     *
     * @return known versions are 2.0, 2.1 and 3.0
     */
	String licenseVersion();

	/**
	 * All code annotated with this annotation is considered to be modified.
	 *
	 * @return true if code was modified (even if just the package was changed)
	 */
	boolean modified() default true;

	/**
	 * @return the short project name
	 */
	String project() default "";

}
