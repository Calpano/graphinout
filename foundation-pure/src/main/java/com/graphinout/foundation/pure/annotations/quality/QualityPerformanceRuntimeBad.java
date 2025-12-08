package com.graphinout.foundation.pure.annotations.quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: If this annotation is present, the annotated method has a runtime, which could be
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.METHOD })
public @interface QualityPerformanceRuntimeBad {
	String comment() default "";
}
