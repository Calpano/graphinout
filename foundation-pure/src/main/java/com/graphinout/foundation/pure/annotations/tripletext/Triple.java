package com.graphinout.foundation.pure.annotations.tripletext;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

/**
 * Documentation: Triple
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
        // Class, interface (including annotation interface), enum, or record; declaration */
        TYPE,

        // Field declaration (includes enum constants) */
        FIELD,

        // Method declaration */
        METHOD,

        // Formal parameter declaration */
        PARAMETER,

        // Constructor declaration */
        CONSTRUCTOR,

        // Local variable declaration */
        LOCAL_VARIABLE,

        // Annotation interface declaration (Formerly known as an annotation type.) */
        ANNOTATION_TYPE,

        // Package declaration */
        PACKAGE,

        // Type parameter declaration; @since 1.8
        TYPE_PARAMETER,

        // Use of a type; @since 1.8
        TYPE_USE,

        // Module declaration.; @since 9
        // MODULE,

        // Record component; @jls 8.10.3 Record Members; @jls 9.7.4 Where Annotations May Appear; @since 16
        // RECORD_COMPONENT
})
@Repeatable(Triples.class)
public @interface Triple {
    String s();
    String p() default "isRelated";
    String o();
}
