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

package jointyc.analysis.parser.exception;

import java.util.Collections;
import java.util.Set;

/**
 * Thrown when a parser has found an unexpected symbol.<br>
 * This exception stores some other information about the event:<br>
 * - the position, in terms of number of characters from position 0, of the unexpected symbol;<br>
 * - the position, in terms of line and column from position 0, of the unexpected symbol;<br>
 * - the string representation of the unexpected symbol;<br>
 * - the source string to be parsed;<br>
 * - the set of expected terminal tokens, each represented by an object of class {@link ExpectedTerminal}.<br>
 * 
 * @author Salvatore Giampà
 *
 */
public class UnexpectedSymbolException extends Exception{
	private static final long serialVersionUID = 8241233218556749002L;
	
	/**
	 * Represents an expected terminal token.<br>
	 * It stores some information about the expected token:<br>
	 * - the position, in terms of characters from position 0, which equals the unexpected symbol's position;
	 * - a description of the expected terminal token (e.g. "if construct" for the token "if").
	 * - the type of the expected token.
	 * 
	 * @author Salvatore Giampà
	 *
	 */
	public static class ExpectedTerminal{
		public final int position;
		public final String description;
		public final String tokenType;
		
		/**
		 * Create a representation for an expected token.
		 * @param position the position at which the symbol is expected
		 * @param tokenType the type of the expected token
		 * @param description a description for the expected token
		 */
		public ExpectedTerminal(int position, String tokenType, String description){
			this.position = position;
			this.tokenType = tokenType;
			this.description = description;
		}
		
		@Override
		public String toString() {
			return description;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((tokenType == null) ? 0 : tokenType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExpectedTerminal other = (ExpectedTerminal) obj;
			if (tokenType == null) {
				if (other.tokenType != null)
					return false;
			} else if (!tokenType.equals(other.tokenType))
				return false;
			return true;
		}
		
		
	}
	
	/**
	 * The set of expected terminal tokens.
	 */
	public final Set<ExpectedTerminal> expected;
	
	/**
	 * The source string.
	 */
	public final CharSequence source;
	
	/**
	 * The unexpected token.
	 */
	public final CharSequence unexpected;
	
	/**
	 * The position in lines of the unexpected token.
	 */
	public final int line;
	
	/**
	 * The position in columns of the unexpected token.
	 */
	public final int column;

	/**
	 * The position in number of characters from position 0 of the unexpected token.
	 */
	public final int position;
	
	/**
	 * Construct the exception.
	 * The position of the unexpected symbol, in terms of lines and columns, 
	 * will be automatically computed using the passed position in characters and the source string.
	 * @param expected the set of expected terminal tokens
	 * @param found the unexpected symbol
	 * @param position the position of the unexpected symbol, in terms of number of characters
	 * @param source the source string to be parsed
	 */
	public UnexpectedSymbolException(Set<ExpectedTerminal> expected, CharSequence found, int position, CharSequence source){
		this.source = source;
		this.unexpected = found!=null? found : "";
		this.expected = Collections.unmodifiableSet(expected);
		this.position = position;
		
		int c = 1, l = 1;
		
		for(int i=0; i<position; i++){
			if(source.charAt(i) == '\n'){
				c=1; l++;
			}
			else c++;
		}

		this.line = l;
		this.column = c;
	}
	
	@Override
	public String toString() {
		int len = unexpected.length();
		
		String unexpected = "";
		
		if(this.unexpected.length() > 70)
			unexpected = this.unexpected.subSequence(0, 30).toString() + " ... " + this.unexpected.subSequence(len-30, len).toString();
		else
			unexpected = this.unexpected.toString();
		
		unexpected.replaceAll("\\n", "\\\\n")
				.replaceAll("\\r", "\\\\r")
				.replaceAll("\\t", "\\\\t");
		
		return "UnexpectedSymbolException [expected: " + expected + "; found: \"" + unexpected + "\"; line: " + line + "; column: " + column + "; position: " + position + "]";
	}
}