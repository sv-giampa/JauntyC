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

package jointyc.analysis.lexer;

import java.util.Set;

/**
 * Defines the interface of a generic lexer.</br>
 * </br>
 * A lexer is thought to recognize the membership of tokens to some "type",
 * defined through some notation, such as regular expressions.</br>
 * </br>
 * A lexer is generically non editable. This interface
 * does not provide any modifier method for the lex rules (see {@link EditableLexer}).</br>
 * </br>
 * A token might match multiple types, and each matched type might
 * refer to a certain token with a certain length.</br>
 * </br>
 * For instance:
 * <ul>
 * 		<li>Three token types could be defined: <ul>
 * 				<li>- "double": matches double precision floating points (e.g. "31.4");
 * 				<li>- "integer": matches integer number (e.g. "15");
 * 				<li>- "ip": matches an Internet Protocol Address (e.g. "192.168.1.1");
 * 				</ul>
 * 
 *  	<li>the token "23.57.72.4":<ul>
 *  	<li>- matches the type "double" as "23.57";
 *  	<li>- matches the type "integer" as "23";
 *  	<li>- matches the type "ip" as "23.57.72.4";
 * </ul>
 * 
 * @author Salvatore Giampà
 *
 */
public interface Lexer extends Cloneable{
	
	/**
	 * Sets the input string for this lexer
	 * @param input the string to lex
	 */
	void setInput(String input);
	
	/**
	 * Recognizes if the last discovered tokens is a member of the specified type group.</br>
	 * This method should be used after a call to the {@link #next()} method.</br>
	 * </br>
	 * For example: <ul>
	 * 			<li>if there are three types, "double", "integer" and "letters", the token "38.5"
	 * 				would match the type "double" as "38.5" and the type "integer" as "38", but not
	 * 				the type "string".</br>
	 * 				Then at position 0 of the token "    38.5  "
	 * 				(note the spaces before character '3' and after character '5'):<ul>
	 * 					<li>the call token("double") will return "38.5";
	 * 					<li>the call token("integer") will return "38";
	 * 					<li>the call token("string") will return null;
	 * 					</ul>
	 * 				</ul>
	 * 
	 * This method is useful to be used by a parser, searching for the next expected token type.
	 * 
	 * @param type the name of the token type
	 * @return the token that matches the type or null if no token was found for the specified type, at the first position where another one matched
	 */
	String token(String type);
	
	/**
	 * Get the token that matches the default type of the matched types.
	 * Generally the default token type is the matched type witch was first added to the lexer rules.
	 * The default type definition could change for some implementations.
	 * @return the token that is member of the default type.
	 */
	String token();
	

	/**
	 * Get the default matched token type.
	 * Generally the default token type is the one of matched types witch was first added to the lexer rules.
	 * The default type definition could change for some implementations.
	 * @return the token that is member of the default type.
	 */
	String tokenType();
	
	/**
	 * Get the set of token types which matched a tokent at the current position.
	 * @return the set of possible matching types
	 */
	Set<String> similarTypes();
	
	/**
	 * Get the start position of all matched types. It is the same for all currently matched type.
	 * @return the start position of the matching
	 */
	int start();
	
	/**
	 * Get the end position of the currently matched type. It is the end of
	 * the token matching the default type just after a call to the {@link #next()} method,
	 * or the length of the token matching the type just passed to the {@link #token(String)} method.
	 * @return the end position of the current matching token
	 */
	int end();
	
	/**
	 * Set the start position of the next computation of the {@link #next()} method.
	 * @param position the start position to set
	 */
	void setStart(int position);
	
	/**
	 * Get the input string setted by the {@link #setInput(String)} method.
	 * @return the input string
	 */
	String input();
	
	/**
	 * Search the next token matching.
	 * @return true if a matching was found for some type, false otherwise
	 */
	boolean next();
	
	
	/**
	 * Get the printable description associated to the specified type.
	 * @param type the type name
	 * @return the description for the specified type
	 */
	String description(String type);
	
	/**
	 * Get the regex associated to the specified type
	 * @param type the type name
	 * @return the regex for the specified type
	 */
	String regex(String type);
}
