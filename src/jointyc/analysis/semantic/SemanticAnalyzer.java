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

package jointyc.analysis.semantic;

import java.util.ArrayList;
import java.util.LinkedList;

import jointyc.analysis.parser.SyntaxIterator;
import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.semantic.exception.AxiomResultException;
import jointyc.analysis.semantic.exception.SemanticException;

/**
 * Implements a visiting machinery of the syntax tree to apply semantic actions, defined by the interpreter
 * associated at construction time.
 * @author Salvatore Giampà
 *
 */
public class SemanticAnalyzer{
	
	public static enum ControlCode{
		TERMINATE
	}
	
	private static class TreeNode{
		SyntaxTree tree;
		boolean expanded = false;
		TreeNode father;
		ArrayList<Object> results = new ArrayList<>();
		
		TreeNode(SyntaxTree tree, TreeNode father){
			this.tree = tree;
			this.father = father;
		}
	}
	
	private Interpreter interpreter;
	
	public SemanticAnalyzer(Interpreter interpreter) {
		if(interpreter == null)
			throw new NullPointerException();
		this.interpreter = interpreter;
	}
	
	/**
	 * Implements the iterative algorithm that visits the specified syntax tree and
	 * calls the semantic actions on the interpreter
	 * @param tree the syntax tree root to analyze
	 * @return the result of semantic analysis or null
	 * @throws SemanticException if the interpreter discovers semantic errors
	 * @throws AxiomResultException if more than one result are returned by the axiom semantic action
	 */
	public Object analyze(SyntaxTree tree) throws SemanticException {
		
		//data structures
		LinkedList<TreeNode> stack = new LinkedList<>();
		Object result;
		
		//put the root in the stack
		TreeNode root = new TreeNode(tree, null);
		stack.push(root);
		
		while(stack.size() > 0) {
			TreeNode node = stack.peek();
			
			if(node.tree.terminal()) {
				
				result = interpreter.terminal(node.tree);
				
				if(result == ControlCode.TERMINATE)
					return null;
				
				if(result != null && node.father != null)
					node.father.results.add(result);
				
				stack.pop();
			}
			else {
				if(node.expanded) {
					interpreter.nonTerminal(node.tree, node.results);
					
					if(node.results.size() > 0 && node.results.get(0) == ControlCode.TERMINATE)
						return null;
					
					if(node.father != null)
						node.father.results.addAll(node.results);
					stack.pop();
				}
				else {
					SyntaxIterator it = node.tree.iteratorFromLast();
					while(it.hasPrevious()) 
						stack.push(new TreeNode(it.previous(), node));
					node.expanded = true;
				}
			}
		}
		
		if(root.results.size() == 0)
			return null;
		
		if(root.results.size() > 1)
			throw new AxiomResultException();
		
		return root.results.remove(0);
	}
	
}
