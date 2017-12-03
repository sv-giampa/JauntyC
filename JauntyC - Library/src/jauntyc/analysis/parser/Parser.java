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

package jauntyc.analysis.parser;

import jauntyc.analysis.lexer.Lexer;
import jauntyc.analysis.parser.exception.UnexpectedSymbolException;

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
}
