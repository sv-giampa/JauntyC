package jointyc.analysis.semantic.exception;

import java.lang.reflect.Method;
import java.util.Arrays;

import jointyc.analysis.semantic.annotation.NonTerminalToken;
import jointyc.analysis.semantic.annotation.TerminalToken;

/**
 * Usually thrown when the same {@link TerminalToken} annotation is repeated for the same token on the same method.
 * @author Salvatore Giampa'
 *
 */
public class TerminalReplicationException extends RuntimeException {
	
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
	 * The terminal token type which caused this exception.
	 */
	private TerminalToken tokenType;

	public TerminalReplicationException(Class<?> interpreterClass, Method interpreterMethod, TerminalToken tokenType) {
		super("The method " + interpreterClass.getCanonicalName() + "." + interpreterMethod.getName() + 
				"() replicates the rule '" + tokenType.value() + "'");
		
		this.interpreterClass = interpreterClass;
		this.interpreterMethod = interpreterMethod;
		this.tokenType = tokenType;
	}

}
