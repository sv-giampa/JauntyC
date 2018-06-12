package jointyc.analysis.semantic.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class UnknownParameterException extends RuntimeException {

	private static final long serialVersionUID = 2727068401067571210L;

	/**
	 * 
	 */
	public final Method method;
	public final Parameter parameter;
	
	public UnknownParameterException(Method method, Parameter parameter) {
		super("Parameter " + parameter + " in method " + method + " is unknown for semantic analysis. "
				+ "Allowed parameters are 'SyntaxTree tree', 'String token', 'String type', "
				+ "'String source', 'int start', 'int end', in the order you prefer");
		this.method = method;
		this.parameter = parameter;
	}

}
