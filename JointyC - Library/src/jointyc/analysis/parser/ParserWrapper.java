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

import jointyc.analysis.lexer.Lexer;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;

/**
 * Wrap an editable parser into a non-editable one.
 * 
 * @author Salvatore Giampà
 *
 */

public class ParserWrapper implements Parser {
	private final EditableParser parser;
	
	/**
	 * Wrap an editable parser into a non-editable one.
	 * @param parser the editable parser to wrap
	 * @return a non-editable parser backed by the specified editable parser
	 */
	public static Parser wrap(EditableParser parser){
		return new ParserWrapper(parser);
	}
	
	private ParserWrapper(EditableParser parser) {
		this.parser = parser;
	}
	
	@Override
	public void setLexer(Lexer lexer) {
		parser.setLexer(lexer);
	}

	@Override
	public Lexer getLexer() {
		return parser.getLexer();
	}

	@Override
	public SyntaxTree parse() throws UnexpectedSymbolException {
		return parser.parse();
	}
	
}
