package com.graphinout.foundation.pure.annotations.quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: There are obvious possibilities to improve the performance. The provided implementation serves as a default implementation, which is Ok to use in production, but can be optimized.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.METHOD})
public @interface QualitySuboptimal {
    String comment() default "";
}
