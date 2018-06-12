package jointyc.analysis.semantic.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Aggreagates repeatable {@link NonTerminalToken} annotations
 * @author Salvatore Giampa'
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface NonTerminalTokens {
	/**
	 * 
	 * @return the aggragated annotations
	 */
	NonTerminalToken[] value();
}
