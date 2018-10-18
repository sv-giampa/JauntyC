/**
 * 
 */
package jointyc.analysis.semantic.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jointyc.analysis.semantic.Interpreter;

/**
 * Annotate an {@link Interpreter} subclass to be bound to a language.<br><br>
 * Example:<br><br>
 * <code>
 * &#64;Language("myLanguage")<br>
 * public class MyLanguageInterpreter implements Interpreter{<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;  &#64;TerminalToken("myToken")<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;  // equal to &#64;TerminalToken("myLanguage.myToken")<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;  private void myToken(){ ... }<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;  ...<br>
 * } //MyLanguageInterpreter<br>
 * </code>
 * @author Salvatore Giampa'
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Language {
	
	/**
	 * 
	 * @return The lexicon token type that must be associated to the annotated interpreter method
	 */
	String value();
}
