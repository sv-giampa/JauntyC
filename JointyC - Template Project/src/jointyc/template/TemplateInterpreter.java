/**
 * LICENSE HERE 
 **/

package jointyc.template;

import java.util.List;

import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.semantic.Interpreter;
import jointyc.analysis.semantic.exception.SemanticException;

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
