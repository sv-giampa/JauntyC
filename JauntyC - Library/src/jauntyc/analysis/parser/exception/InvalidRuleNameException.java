/**
 *  Copyright 2017 Salvatore Giamp�
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

package jauntyc.analysis.parser.exception;

/**
 * Thrown when a parser rule name is invalid or malformed.
 * @author Salvatore Giamp�
 *
 */
public class InvalidRuleNameException extends Exception {
	private static final long serialVersionUID = 1048057964324876422L;
	public final String invalidName;
	
	/**
	 * Constructs the exception.
	 * @param invalidName the invalid rule name
	 * @param namePattern the correct rule name pattern to be matched (usually a regular expression)
	 */
	public InvalidRuleNameException(String invalidName, String namePattern){
		super(
				"invalid rule name \"" + invalidName + "\". "
				+ "A rule name must match the following pattern " + namePattern
			);
		this.invalidName = invalidName;
	}
}
