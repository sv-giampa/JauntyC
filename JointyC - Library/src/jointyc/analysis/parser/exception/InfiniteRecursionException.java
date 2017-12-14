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

/**
 * Thrown when an infinite recursion is detected while constructing a parser
 * @author Salvatore Giampà
 *
 */
public class InfiniteRecursionException extends Exception {
	private static final long serialVersionUID = -2560003102211945612L;
	
	/**
	 * the rule head
	 */
	public final String rule;
	
	/**
	 * the first of the rule
	 */
	public final String first;
	
	/**
	 * Construct the exception.
	 * @param rule the rule head
	 * @param first the first of the rule
	 */
	public InfiniteRecursionException(String rule, String first){
		super(String.format("infinite left recursion detected for rule: \"%s = %s ...\"", rule, first));
		this.rule = rule;
		this.first = first;
	}
}
