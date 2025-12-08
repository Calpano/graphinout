package com.graphinout.foundation.pure.annotations.quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: If this annotation is present, the annotated method is not yet ready.
 * Its the same quality as @Deprecated, but in time before the method is ready to be used.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.METHOD })
public @interface QualityUnfinished {
	String comment() default "";
}
