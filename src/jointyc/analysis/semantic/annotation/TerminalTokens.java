package jointyc.analysis.semantic.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Aggreagates repeatable {@link TerminalToken} annotations
 * @author Salvatore Giampa'
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface TerminalTokens {
	/**
	 * 
	 * @return the aggragated annotations
	 */
	TerminalToken[] value();
}
