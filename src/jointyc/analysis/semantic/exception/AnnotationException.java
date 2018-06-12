/**
 *  Copyright 2017 Salvatore Giampà
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 **/

package jointyc.analysis.semantic.exception;

import java.lang.reflect.Method;

import jointyc.analysis.semantic.annotation.NonTerminalToken;
import jointyc.analysis.semantic.annotation.TerminalToken;

/**
 * Thrown when an interpretation method is annotated for a non-existing token (terminal or non-terminal)
 * with the {@link TerminalToken} or with the {@link NonTerminalToken} annotations.
 * @author Salvatore Giampà
 *
 */
public class AnnotationException extends RuntimeException {
	private static final long serialVersionUID = 1021579297957230596L;

	/**
	 * The class of the interpreter which caused this exception.
	 */
	public final Class<?> interpreterClass;

	/**
	 * The method of the interpreter which caused this exception.
	 */
	public final Method interpreterMethod;
	
	/**
	 * The non-existing token type which caused this exception.
	 */
	public final String tokenType;
	
	public AnnotationException(Class<?> interpreterClass, Method interpreterMethod, String tokenType) {
		super("The method " + interpreterClass.getCanonicalName() + "." + interpreterMethod.getName() + "() is annotated for the non-existing token type \""+tokenType +"\".");
		this.interpreterClass = interpreterClass;
		this.interpreterMethod = interpreterMethod;
		this.tokenType = tokenType;
	}
	
	
}
