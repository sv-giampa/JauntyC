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

package jointyc.analysis.parser;

import java.util.List;

import jointyc.analysis.lexer.Lexer;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;

/**
 * Defines a generic parser.
 * @author Salvatore Giampà
 *
 */
public interface Parser {
	
	/**
	 * Sets the lexer for this parser.
	 * @param lexer the lexer to use.
	 */
	void setLexer(Lexer lexer);
	
	/**
	 * Gets the lexer used by this parser.
	 * @return the lexer used
	 */
	Lexer getLexer();
	
	/**
	 * Start the parsing process.
	 * @return a {@link SyntaxTree} if the parsing was successful, false otherwise
	 * @throws UnexpectedSymbolException if an unexpected symbol was found.
	 */
	SyntaxTree parse() throws UnexpectedSymbolException;
	
	/**
	 * Gets the productions associated to the specified rule head.
	 * The returned production lists adhere to the logic of {@link EditableParser#addRule(String, String...)}, to distinguish terminal tokens from non-terminal ones.
	 * @param head the head of the rule
	 * @return the list of production lists
	 */
	List<List<String>> getRule(String head);
	
	String getAxiom();
	
	/**
	 * Check the existence of at least a production rule for the specified non-terminal token.
	 * @param head the head of the productions rules to be checked. It is a non-terminal token.
	 * @param production the elements of the production body
	 * @return true if the specified non-terminal token produces something, false otherwise.
	 */
	boolean ruleExists(String head, String... production);
}
