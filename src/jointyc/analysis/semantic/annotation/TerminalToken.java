/**
 * 
 */
package jointyc.analysis.semantic.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotate a method for the interpretation of terminal token
 * @author Salvatore Giampa'
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(value = TerminalTokens.class)
public @interface TerminalToken {
	
	/**
	 * 
	 * @return The lexicon token type that must be associated to the annotated interpreter method
	 */
	String type();
}
