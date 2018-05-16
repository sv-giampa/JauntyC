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
 * Wrap an editable lexer into a non-editable one.
 * @author Salvatore Giampà
 *
 */
public class LexerWrapper implements Lexer {

	private EditableLexer lexer;
	
	public static Lexer wrap(EditableLexer lexer){
		return new LexerWrapper(lexer);
	}
	
	private LexerWrapper(EditableLexer lexer){
		this.lexer = lexer;
	}
	
	@Override
	public void setInput(String input) {
		lexer.setInput(input);
	}

	@Override
	public String token(String type) {
		return lexer.token(type);
	}
	
	@Override
	public String token() {
		return lexer.token();
	}

	@Override
	public String tokenType() {
		return lexer.tokenType();
	}

	@Override
	public int start() {
		return lexer.start();
	}
	
	@Override
	public int end() {
		return lexer.end();
	}

	@Override
	public void setStart(int position) {
		lexer.setStart(position);
	}

	@Override
	public boolean next() {
		return lexer.next();
	}

	@Override
	public String input() {
		return lexer.input();
	}

	@Override
	public Set<String> similarTypes() {
		return lexer.similarTypes();
	}

	@Override
	public String description(String type) {
		return lexer.description(type);
	}

	@Override
	public String regex(String type) {
		return lexer.regex(type);
	}

}
