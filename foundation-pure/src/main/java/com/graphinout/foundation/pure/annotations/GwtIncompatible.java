package com.graphinout.foundation.pure.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: Any class, method or field with an annotation @GwtIncompatible (with any package prefix) is ignored by
 * the GWT compiler.
 * <p>
 * Impl: Since only the name of the annotation matters, Java libraries may use their own copy of this annotation class
 * to avoid adding a compile-time dependency on GWT.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR })
@Documented
public @interface GwtIncompatible {
	String value();
}
