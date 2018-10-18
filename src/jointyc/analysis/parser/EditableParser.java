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

import jointyc.analysis.parser.exception.InfiniteRecursionException;
import jointyc.analysis.parser.exception.InvalidRuleNameException;

/**
 * Defines a generic editable parser.
 * This interface exposes methods to add rules to the parser implementation.
 * @author Salvatore Giampà
 *
 */
public interface EditableParser extends Parser{
	
	/**
	 * The prefix used to distinguish the terminal products by the non-terminal ones.
	 */
	public final String TERMINAL_PREFIX = "$";
	
	/**
	 * Regular expression matching the {@link TERMINAL_PREFIX}
	 */
	public final String TERMINAL_PREFIX_REGEX = "(\\$)";
	
	/**
	 * The pattern that must be matched by rule names
	 */
	public final String RULE_PATTERN = "[a-zA-Z][a-zA-Z0-9_\\.\\#]*";

	/**
	 * Adds a new rule to this parser.<br>
	 * The production parameter might contain, indifferently, terminal and non-terminal symbols.<br>
	 * Terminal and non-terminal symbols are distinguished by adding the prefix specified by {@link #TERMINAL_PREFIX} to the product name.<br>
	 * (for example: {@link #TERMINAL_PREFIX} + "double", specifies that the type "double" is terminal.<br>
	 * <br>
	 * A rule name must match the regex specified by {@link #RULE_PATTERN}<br>
	 * @param rule the production head
	 * @param production the production tail
	 * @throws InfiniteRecursionException if the specified rule closes an infinite left recursion chain<br>
	 * (e.g. A-&#62;Ba; B-&#62;Cb; C-&#62;A c; the last rule will throw an {@link InfiniteRecursionException})
	 * @throws InvalidRuleNameException if the rule name is not valid, according to the pattern {@link #RULE_PATTERN}
	 */
	void addRule(String rule, String... production) throws InfiniteRecursionException, InvalidRuleNameException;
	

	/**
	 * The same of {@link #addRule(String, String...)}, but accepting a list for production.<br>
	 * @param rule the production head
	 * @param production the production tail
	 * @throws InfiniteRecursionException if the specified rule closes an infinite recursion chain<br>
	 * (e.g. A-&#62;Ba; B-&#62;Cb; C-&#62;Ac; the last rule will throw an {@link InfiniteRecursionException})
	 * @throws InvalidRuleNameException if the rule name is not valid, according to the pattern {@link #RULE_PATTERN}
	 */
	void addRule(String rule, List<String> production) throws InfiniteRecursionException, InvalidRuleNameException;
	
	/**
	 * Sets the axiom of the grammar to the specified non-terminal symbol.<br>
	 * In other words, sets the starting rule head for strings generations.<br>
	 * @param axiom the starting non-terminal symbol.
	 */
	void setAxiom(String axiom);
}
