package jointyc.analysis.semantic.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.semantic.SemanticAnalyzer;

/**
 * Annotate a method for the interpretation of a non-terminal token.
 * @author Salvatore Giampa'
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(NonTerminalTokens.class)
public @interface NonTerminalToken {
	
	/**
	 * 
	 * @return The head of the grammar rule associated to the interpreter method
	 */
	String ruleHead();
	
	/**
	 * A partial production sequence. It should be used to distinguish the different grammar rules with the same head, 
	 * indicating a list of production tokens from the first of the rule to the first token that differs.
	 * This annotation is used by the {@link SemanticAnalyzer} together with the {@link SyntaxTree#query(String, String...)} method
	 * @return the partial production sequence of the grammar rule
	 */
	String[] ruleProduction() default {};
}
