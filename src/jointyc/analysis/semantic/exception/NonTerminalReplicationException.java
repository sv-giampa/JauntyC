package jointyc.analysis.semantic.exception;

import java.lang.reflect.Method;
import java.util.Arrays;

import jointyc.analysis.semantic.annotation.NonTerminalToken;
import jointyc.analysis.semantic.annotation.TerminalToken;

/**
 * Usually thrown when the same {@link NonTerminalToken} annotation is repeated for the same token on the same method.
 * @author Salvatore Giampa'
 *
 */
public class NonTerminalReplicationException extends RuntimeException {
	
	private static final long serialVersionUID = 1822939264223404867L;

	/**
	 * The class of the interpreter which caused this exception.
	 */
	public final Class<?> interpreterClass;

	/**
	 * The method of the interpreter which caused this exception.
	 */
	public final Method interpreterMethod;

	/**
	 * The non-terminal token type which caused this exception.
	 */
	public final NonTerminalToken tokenType;

	public NonTerminalReplicationException(Class<?> interpreterClass, Method interpreterMethod, NonTerminalToken tokenType) {
		super("The method " + interpreterClass.getCanonicalName() + "." + interpreterMethod.getName() + 
				"() replicates the query '" + tokenType.ruleHead() + " = " + Arrays.toString(tokenType.ruleProduction()) + "'");

		this.interpreterClass = interpreterClass;
		this.interpreterMethod = interpreterMethod;
		this.tokenType = tokenType;
	}

}
