package jointyc.analysis.semantic.exception;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MutuallyExclusiveInterpretationsException extends RuntimeException {

	private static final long serialVersionUID = -6253078713073658769L;
	
	/**
	 * The method that caused this exception
	 */
	public final Method method;

	public MutuallyExclusiveInterpretationsException(Method method) {
		super("@TerminalToken and @NonTerminalToken must be mutually exclusive on the method '" + method + "'");
		this.method = method;
	}

}
