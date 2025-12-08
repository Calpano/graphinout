package com.graphinout.foundation.pure.annotations.quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: The annotated type/method has been reviewed and has acceptable memory and runtime characteristics.
 * There are no obvious improvements to be made.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.METHOD })
public @interface QualityOK {
	String comment() default "";
}
