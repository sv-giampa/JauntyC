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

import java.util.Set;

import jointyc.analysis.lexer.Lexer;

/**
 * Defines a recursive data structure, an m-ary tree, used to represent the
 * source string, according to the grammar implemented by a parser. 
 * This data structure is recursively iterable, to simplify a semantic analysis.
 * 
 * @author Salvatore Giampà
 *
 */
public interface SyntaxTree extends Iterable<SyntaxTree> {

	SyntaxIterator iterator();
	
	SyntaxIterator iteratorFromLast();
	
	/**
	 * Verify if this node represents a terminal token.
	 * 
	 * @return true if it is a terminal token, false otherwise
	 */
	public boolean terminal();

	/**
	 * Gets the primary type for this token
	 * 
	 * @return the primary type assigned by the lexer
	 */
	public String type();

	/**
	 * Gets the set of similar types matched by this token. The set contains
	 * also the primary type.
	 * 
	 * @return a non-modifiable set of the similar types
	 */
	public Set<String> similarTypes();

	/**
	 * Gets the number of children of this node.
	 * 
	 * @return the number of children
	 */
	public int nexts();

	/**
	 * Gets the position at which this token starts.
	 * 
	 * @return the start position of this token
	 */
	public int start();

	/**
	 * Gets the position at which this token ends.
	 * 
	 * @return the end position of this token
	 */
	public int end();

	/**
	 * The characters string extracted by the source string for this token.
	 * 
	 * @return the token itself as string
	 */
	public String token();

	/**
	 * Gets the source string the token was extracted from.
	 * 
	 * @return the parsed source string
	 */
	public String source();

	/**
	 * Gets the lexer used for the parsing.
	 * 
	 * @return the lexer used by the parser
	 */
	public Lexer lexer();
	
	
	/**
	 * Queries for a rule start part. Checks if this tree has a certain type, and
	 * then proceeds testing the types of the first k children, where k is the
	 * size of the production array specified.</br>
	 * </br>
	 * Terminal and non-terminal symbols are distinguished in the production, by the prefix specified by {@link EditableParser#TERMINAL_PREFIX}.</br>    
	 * See the logic of {@link EditableParser#addRule(String, String...)} for more information.</br>
	 * </br>
	 * If the specified production length is exactly 1 and its first element (production[0]) is equal to "#", then returns true this tree has some next, false otherwise.</br>    
	 * </br>
	 * If the specified production length is exactly 1 and its first element (production[0]) is equal to "!#", then returns true this tree has no next, false otherwise.</br>    
	 * </br>
	 * </br>
	 * Example: (assume the character # is the empty string)</br>
	 * 
	 * Consider the following production rules:</br>
	 * <code>a -> b c d | x y z | x f g</code></br>
	 * <code>b -> p q | #</code></br>
	 * And consider the following declaration:</br>
	 * <code>SyntaxTree tree;</code></br>
	 * </br>
	 * The following are true:</br>
	 * <ul>
	 * <li><code>tree.query("a", "x")</code> will return true if tree is of type "a" and its first next is of type "x".</br>
	 * It will return true if the matching rule is "<code>a -> x y z</code>" or "<code>a -> x f g</code>"
	 * 
	 * <li><code>tree.query("a", "x", "y")</code> will return true if tree is of type "a" and its first next is of type "x" and its second next is of type "y".</br>
	 * It will return true if and only if the rule "<code>a -> x y z</code>" matched.
	 * 
	 * <li><code>tree.query("b", "#")</code> will return true if tree is of type "b" and it has no next.</br>
	 * It will return true if and only if the rule "<code>b -> #</code>" matched.
	 * 
	 * <li><code>tree.query("a", "#")</code> will return true if tree is of type "a" and it has no next.</br>
	 * It will never return true, because the rule "<code>a -> #</code>" is not defined.
	 * 
	 * <li><code>tree.query("b", "!#")</code> will return true if tree is of type "b" and it has some next.</br>
	 * It will return true if and only if the rule "<code>b -> p q</code>" matched.
	 * 
	 * <li><code>tree.query("a", "!#")</code> will return true if tree is of type "a" and it has some next.</br>
	 * It will always return true, because the rule "<code>a -> #</code>" is not defined.
	 * </ul>
	 * 
	 * @param type the type of this syntax node
	 * @param production the types of the first k children of this tree
	 * @return true if the query is successful, false otherwise
	 */
	public default boolean query(String type, String... production) {
		if (type == null || !type.equals(type()))
			return false;
		
		if(production.length == 1){
			if(production[0].equals("#"))
				return nexts()==0;
			if(production[0].equals("!#"))
				return nexts()>0;
		}
		
		if (production.length > nexts())
			return false;

		SyntaxIterator it = iterator();
		for (int i = 0; i < production.length; i++) {
			if (!it.hasNext())
				return false;
			
			String prodType;
			boolean prodTerminal = production[i].startsWith(EditableParser.TERMINAL_PREFIX);
			if(prodTerminal)
				prodType = production[i].substring(1);
			else
				prodType = production[i];
			
			SyntaxTree nextType = it.next();
			if (prodType == null || !prodType.equals(nextType.type()) || prodTerminal != nextType.terminal()) {
				return false;
			}
		}

		return true;
	}
}
