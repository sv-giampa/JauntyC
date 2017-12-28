package jointyc.tutorial.list;

import java.util.LinkedList;
import java.util.List;

import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.semantic.Interpreter;
import jointyc.analysis.semantic.exception.SemanticException;

public class ListInterpreter implements Interpreter {

	@Override
	public Object terminal(SyntaxTree tree) throws SemanticException {
		switch(tree.type()) {
		case "listTut.string":
			String string =  tree.token();
			return string.substring(1, string.length()-1);
		case "listTut.number":
			return Double.valueOf(tree.token());
		}
		return null;
	}

	@Override
	public void nonTerminal(SyntaxTree tree, List<Object> resultsBuffer) throws SemanticException {
		if(tree.query("listTut.homoList", "$listTut.homoOpen", "$listTut.string")) {
			List<String> list = new LinkedList<>();
			for(Object o : resultsBuffer) 
				list.add((String)o);
			resultsBuffer.clear();
			resultsBuffer.add(list);
		}
		else if(tree.query("listTut.homoList", "$listTut.homoOpen", "$listTut.number")) {
			List<Double> list = new LinkedList<>();
			for(Object o : resultsBuffer) 
				list.add((Double)o);
			resultsBuffer.clear();
			resultsBuffer.add(list);
		}
		else if(tree.query("listTut.homoList", "$listTut.homoOpen", "listTut.list")) {
			List<List<?>> list = new LinkedList<>();
			for(Object o : resultsBuffer) 
				list.add((List<?>)o);
			resultsBuffer.clear();
			resultsBuffer.add(list);
			
		}
		else if(tree.query("listTut.homoList", "$listTut.homoOpen", "$listTut.homoClose")) {
			List<?> list = new LinkedList<>();
			resultsBuffer.add(list);
		}
		else if(tree.query("listTut.heteroList", "$listTut.heteroOpen", "listTut.value")) {
			List<Object> list = new LinkedList<>();
			list.addAll(resultsBuffer);
			resultsBuffer.clear();
			resultsBuffer.add(list);
		}
		else if(tree.query("listTut.heteroList", "$listTut.heteroOpen", "$listTut.heteroClose")) {
			List<?> list = new LinkedList<>();
			resultsBuffer.add(list);
		}
	}

}
