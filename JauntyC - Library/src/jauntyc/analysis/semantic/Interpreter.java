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

package jauntyc.analysis.semantic;

import java.util.List;

import jauntyc.analysis.parser.SyntaxTree;
import jauntyc.analysis.semantic.exception.SemanticException;

/**
 * Basing on the Template Method pattern, the implementors of this interface must provide the semantic actions
 * for the terminal and non-terminal symbols in the syntax tree. Applying a semantic action could lead to the production
 * of some semantic result. Terminal symbols' semantic result should be returned by the {@link #terminal(SyntaxTree)} method.
 * Non-terminal semantic actions receive the semantic result of deeper nodes in a buffer (see {@link #nonTerminal(SyntaxTree, List)}) and
 * returns their semantic results in the same buffer, eventually removing the received ones.
 * 
 * @author Salvatore Giampà
 *
 */
public interface Interpreter {
	
	/**
	 * Apply semantics to a terminal token specified in the lexer types.
	 * @param tree the syntax node relative to the terminal symbol
	 * @return the semantic result of the node, if any, null otherwise
	 * @throws SemanticException if some semantic actions went wrong.
	 */
	public Object terminal(SyntaxTree tree) throws SemanticException;
	
	/**
	 * Apply semantics to a non terminal syntax node specified in the parser rules.
	 * A non-terminal node result could depend by the results of deeper nodes. These results are passed
	 * into the resultsBuffer. The semantic results of the current non-terminal node are returned in the same resultsBuffer.
	 * If the current computation consumed some result passed, it is his responsibility to remove them from the buffer.
	 * If the current node has no semantic action associated with it, the result buffer should remain untouched, until another semantic action
	 * does not compute it. See the manual of the library for other details.
	 * @param tree the syntax node relative to the terminal symbol
	 * @param resultsBuffer the buffer of deeper nodes results, and current node results.
	 * @throws SemanticException if some semantic actions went wrong.
	 */
	public void nonTerminal(SyntaxTree tree, List<Object> resultsBuffer) throws SemanticException;
}
