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

package jauntyc.template;

import java.util.List;

import jauntyc.analysis.parser.SyntaxTree;
import jauntyc.analysis.semantic.Interpreter;
import jauntyc.analysis.semantic.exception.SemanticException;

public class TemplateInterpreter implements Interpreter{

	@Override
	public Object terminal(SyntaxTree tree) throws SemanticException {
		switch(tree.type()) {
		case "template.templateTerminal":
			/*
			 * return a representation for the terminal token
			 */
			
			System.out.println("    [interpreter]: templateTerminal = " + tree.token()); //first print
			return tree.token();
		}
		return null;
	}

	@Override
	public void nonTerminal(SyntaxTree tree, List<Object> resultsBuffer) throws SemanticException {
		
		if(tree.query("template.axiom")) {
			/*
			 * When the axiom is reached, the last semantic operations are applying
			 * 
			 * FIRST: put final operations here (e.g. close streams, close files, compute the final object, etc...)
			 * 
			 * SECOND: return the final result or object (e.g. a final representation of a program to send to a code generator)
			 */
			
			System.out.println("    [interpreter]: axiom semantics"); //last print
		}
		else if(tree.query("template.templateRule")) {
			/* 
			 * do some semantic operation for this rule
			 * (e.g. compute a partial result or representation, create sub-objects, define fields for the final object, etc...)
			 */
			
			System.out.println("    [interpreter]: templateRule semantics"); //second print
		}
	}

}
