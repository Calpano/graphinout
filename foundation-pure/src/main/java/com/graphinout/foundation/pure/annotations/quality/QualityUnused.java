package com.graphinout.foundation.pure.annotations.quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: If this annotation is present, the annotated method is not used anywhere.
 * Maybe it should be removed. If it is planned to be used, it should be tagged as, e.g., {@link QualityUnchecked}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.METHOD })
public @interface QualityUnused {
	String comment() default "";
}
