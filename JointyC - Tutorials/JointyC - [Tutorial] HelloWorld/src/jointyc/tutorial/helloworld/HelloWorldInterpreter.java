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

package jointyc.tutorial.helloworld;

import java.util.List;

import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.semantic.Interpreter;
import jointyc.analysis.semantic.exception.SemanticException;

public class HelloWorldInterpreter implements Interpreter{

	@Override
	public Object terminal(SyntaxTree tree) throws SemanticException {
		switch(tree.type()) {
		case "HelloWorld.hello":
			System.out.print("hello ");
			break;
		case "HelloWorld.world":
			System.out.print("world");
			break;
		case "HelloWorld.exclamation":
			System.out.print("!");
			break;
		}
		return null;
	}

	@Override
	public void nonTerminal(SyntaxTree tree, List<Object> resultsBuffer) throws SemanticException {
		
		//is this a non empty non-terminal "hello" node? (specific "hello" semantics)
		if(tree.query("HelloWorld.hello", "!#")) {
			System.out.println("the world has been greeted :-D");
		}
		
		//is this a non-terminal "hello" node? (must be verified after specific "hello" semantics)
		else if(tree.query("HelloWorld.hello")) {
			System.out.println("no greeting to the world :-(");
		}
		
		//is this an empty non-terminal "exclamation" node?
		else if(tree.query("HelloWorld.exclamation", "#")) {
			System.out.println("\ngreeting not enthusiastic :-(");
		}
		
		//is this a non-terminal "exclamation" with a terminal "exclamation" token as production first?
		else if(tree.query("HelloWorld.exclamation", "$HelloWorld.exclamation")) {
			System.out.println("\nenthusiastic greeting :-D");
		}
	}

}
