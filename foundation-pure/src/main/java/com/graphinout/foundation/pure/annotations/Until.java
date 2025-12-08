package com.graphinout.foundation.pure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: Until when was the annotated code piece in use. Use a version identifier or an ISO time stamp. Use the
 * last identifier when the code piece was in use.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE })
public @interface Until {
	String value();
}
