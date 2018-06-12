package jointyc.analysis.semantic.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used rarely to indicate that returning from the annotated method, the results buffer must not be cleared before
 * adding the result produced by the method to the results buffer.
 * @author Salvatore Giampa'
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface NoBufferClear {}
