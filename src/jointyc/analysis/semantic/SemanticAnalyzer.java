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

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.lang.model.type.PrimitiveType;

import jointyc.analysis.lexer.Lexer;
import jointyc.analysis.parser.EditableParser;
import jointyc.analysis.parser.Parser;
import jointyc.analysis.parser.StandardParser;
import jointyc.analysis.parser.SyntaxIterator;
import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.semantic.annotation.Language;
import jointyc.analysis.semantic.annotation.NoBufferClear;
import jointyc.analysis.semantic.annotation.NonTerminalToken;
import jointyc.analysis.semantic.annotation.NonTerminalTokens;
import jointyc.analysis.semantic.annotation.TerminalToken;
import jointyc.analysis.semantic.annotation.TerminalTokens;
import jointyc.analysis.semantic.exception.AnnotationException;
import jointyc.analysis.semantic.exception.AxiomBufferClearException;
import jointyc.analysis.semantic.exception.MutuallyExclusiveInterpretationsException;
import jointyc.analysis.semantic.exception.NonTerminalReplicationException;
import jointyc.analysis.semantic.exception.SemanticException;
import jointyc.analysis.semantic.exception.TerminalReplicationException;
import jointyc.analysis.semantic.exception.UnknownParameterException;

/**
 * Implements a visiting machinery of the syntax tree to apply semantic actions,
 * defined by the interpreter associated at construction time.
 * 
 * @author Salvatore Giampà
 *
 */
public class SemanticAnalyzer {

	public static enum ControlCode {
		TERMINATE
	}

	private static class TreeNode {
		SyntaxTree tree;
		boolean expanded = false;
		TreeNode father;
		ArrayList<Object> results = new ArrayList<>();

		TreeNode(SyntaxTree tree, TreeNode father) {
			this.tree = tree;
			this.father = father;
		}
	}

	private static class Interpretation implements Comparable<Interpretation> {
		int priority; //priority of the interpreter
		Interpreter interpreter;
		Method method;
		String ruleHead;
		String[] ruleProduction;

		public Interpretation(Interpreter interpreter, Method method, int priority) {
			this(interpreter, method, priority, null);
		}
		
		public Interpretation(Interpreter interpreter, Method method, int priority, NonTerminalToken ntt) {
			this.interpreter = interpreter;
			this.method = method;
			this.priority = priority;
			
			if(ntt != null) {
				Language languageAnnotation = interpreter.getClass().getAnnotation(Language.class);
				String language;
				if(languageAnnotation != null)
					language = languageAnnotation.value() + ".";
				else
					language = "";
				
				ruleHead = ntt.ruleHead();
				if(!ruleHead.contains("."))
					ruleHead = language + ruleHead;
				
				String[] originalProd = ntt.ruleProduction();
				ruleProduction = new String[originalProd.length];
				for(int i=0; i<originalProd.length; i++) {
					if(!originalProd[i].contains("."))
						if(originalProd[i].startsWith(EditableParser.TERMINAL_PREFIX))
							ruleProduction[i] = "$" + language + originalProd[i].substring(1);
						else
							ruleProduction[i] = language + originalProd[i];
					else
						ruleProduction[i] = originalProd[i];
				}
			}
		}
		
		public String[] ruleProduction() {
			return ruleProduction;
		}
		
		public String ruleHead() {
			return ruleHead;
		}

		public Object invoke(Object... args)
				throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			boolean accessible = method.isAccessible();
			method.setAccessible(true);
			Object res = method.invoke(interpreter, args);
			method.setAccessible(accessible);
			return res;
		}

		@Override
		public int compareTo(Interpretation i) {
			return ruleProduction.length > i.ruleProduction.length ? 1
					: ruleProduction.length < i.ruleProduction.length ? -1
							: priority > i.priority ? 1 : priority < i.priority ? -1 : 0;
		}
	}

	private Parser parser;

	public SemanticAnalyzer(Interpreter interpreter, Parser parser) {
		if (interpreter == null)
			throw new NullPointerException();
		this.parser = parser;
		createInterpretationsMaps(interpreter, 0);
	}

	private void createInterpretationsMaps(Interpreter interpreter, int priority) {
		addAnnotationsEntries(interpreter, priority);

		Field[] fields = interpreter.getClass().getDeclaredFields();

		for (Field f : fields) {
			if (Interpreter.class.isAssignableFrom(f.getType())) {
				try {
					boolean accessible = f.isAccessible();
					f.setAccessible(true);
					Interpreter son = (Interpreter) f.get(interpreter);
					createInterpretationsMaps(son, priority + 1);
					f.setAccessible(accessible);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void addTerminalTokens(String languagePrefix, Interpreter interpreter, int priority, Method method) {
		TerminalToken[] terminalTokens = method.getDeclaredAnnotationsByType(TerminalToken.class);
		
		// add entries to the terminals' map
		for (TerminalToken tt : terminalTokens) {

			validateTerminalMethod(method);
			
			String type = tt.value();
			if(!type.contains("."))
				type = languagePrefix + type;

			if (terminalMap.containsKey(type)) {
				if (terminalMap.get(type).interpreter == interpreter) {
					System.out.println("*************************************");
					continue;
				}
				if (terminalMap.get(type).priority >= priority)
					continue;
			}

			if (parser.getLexer().regex(type) == null) {
				throw new AnnotationException(interpreter.getClass(), method, type);
			}

			terminalMap.put(type, new Interpretation(interpreter, method, priority));
		}
	}

	void addNonTerminalTokens(Interpreter interpreter, int priority, Method method) {
		NoBufferClear noBufferClear = method.getAnnotation(NoBufferClear.class);
		NonTerminalToken[] nonTerminalTokens = method.getDeclaredAnnotationsByType(NonTerminalToken.class);

		// add enties to the non-terminals' map
		for (NonTerminalToken ntt : nonTerminalTokens) {
			Interpretation newInt = new Interpretation(interpreter, method, priority, ntt);

			if (noBufferClear != null && newInt.ruleHead().equals(parser.getAxiom())) {
				throw new AxiomBufferClearException();
			}

			if (!parser.ruleExists(newInt.ruleHead())) {
				throw new AnnotationException(interpreter.getClass(), method, newInt.ruleHead());
			}

			if (!nonTerminalMap.containsKey(newInt.ruleHead()))
				nonTerminalMap.put(newInt.ruleHead(), new ArrayList<>());

			List<Interpretation> ntMethods = nonTerminalMap.get(newInt.ruleHead());
			
			// sorts the methods from the most particular to the most general
			//ntMethods.sort(null);

			
			boolean added = false;
			for (ListIterator<Interpretation> it = ntMethods.listIterator(); it.hasNext();) {
				Interpretation currentInt = it.next();

				boolean equals = false;
				if (newInt.ruleProduction().length == currentInt.ruleProduction().length) {
					equals = true;
					for (int i = 0; i < ntt.ruleProduction().length; i++) {
						String p1 = newInt.ruleProduction()[i];
						String p2 = currentInt.ruleProduction()[i];
						if (!p1.equals(p2)) {
							equals = false;
							break;
						}
					}
					if (equals && currentInt.interpreter == interpreter) {
						throw new NonTerminalReplicationException(interpreter.getClass(), method, ntt);
					}
				}
				
				if (newInt.ruleProduction().length > currentInt.ruleProduction().length) {
					//the interpretation is more particular than the current one
					it.previous();
					it.add(newInt);
					added = true;
					break;
				} else if (newInt.ruleProduction().length <= currentInt.ruleProduction().length && !equals) {
					//the interpretation is more general than the current one
					it.add(newInt);
					added = true;
					break;

				} else if (equals && currentInt.priority > priority) {
					/*
					 * the interpretation must override the current one 
					 * (this case should never be verified, because the interpreters hierarchy
					 * is explored from top (larger priorities) to down (smaller priorities)
					**/
					it.remove();
					it.add(newInt);
					added = true;
					break;
				} else if (equals && currentInt.priority <= priority) {
					//an interpretation of the same priority was already added, then only mark as added
					added = true;
					break;
				}
				
			}

			if (!added)
				ntMethods.add(newInt);

		}
	}

	private void validateTerminalMethod(Method method) {
		Parameter[] parameters = method.getParameters();
	
		if (parameters.length > 0) {
			boolean found = false; //indicates if the SyntaxTree argument has already been found
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i].getType().equals(SyntaxTree.class)) {
					if (found)
						throw new UnknownParameterException(method, parameters[i]);
					found = true;
				} else {
					throw new UnknownParameterException(method, parameters[i]);
				}
			}
		}
	}

	private Map<String, Interpretation> terminalMap = new HashMap<>();
	private Map<String, List<Interpretation>> nonTerminalMap = new HashMap<>();

	private void addAnnotationsEntries(Interpreter interpreter, int priority) {
		Language languageAnnotation = interpreter.getClass().getAnnotation(Language.class);
		String languagePrefix;
		if(languageAnnotation != null)
			languagePrefix = languageAnnotation.value() + ".";
		else
			languagePrefix = "";
		Method[] methods = interpreter.getClass().getDeclaredMethods();

		for (Method m : methods) {
			TerminalToken[] tts = m.getAnnotationsByType(TerminalToken.class);
			NonTerminalToken[] ntts = m.getAnnotationsByType(NonTerminalToken.class);

			if (tts.length>0 && ntts.length > 0) {
				throw new MutuallyExclusiveInterpretationsException(m);
			}

			if (tts.length > 0)
				addTerminalTokens(languagePrefix, interpreter, priority, m);
			if (ntts.length > 0)
				addNonTerminalTokens(interpreter, priority, m);
		}
	}

	private Object invokeTerminalMethod(SyntaxTree tree) throws SemanticException {
		Interpretation interpretation = terminalMap.get(tree.type());
		if (interpretation == null)
			return null;// interpreter.terminal(tree);

		try {
			Parameter[] parameters = interpretation.method.getParameters();
			if (parameters.length > 0)
				return interpretation.invoke(tree);
			else
				return interpretation.invoke();

			// errore
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			throw new SemanticException(e.getTargetException(), tree);
		}

		return null;

	}

	/**
	 * Implements the Java Reflection stuff to call the correct method for a
	 * non-terminal token
	 * 
	 * @param tree
	 * @param resultsBuffer
	 * @throws SemanticException
	 */
	private void invokeNonTerminalMethod(SyntaxTree tree, List<Object> resultsBuffer) throws SemanticException {

		List<Interpretation> interpretations = nonTerminalMap.get(tree.type());
		Object result = null;
		Object[] params = null;
		Interpretation current = null;

		try {
			if (interpretations != null) {
				for (Interpretation interpretation : interpretations) {
					current = interpretation;
					if (tree.query(interpretation.ruleHead(), interpretation.ruleProduction())) {
						boolean noBufferClear = interpretation.method.getAnnotation(NoBufferClear.class) != null;
						Parameter[] mParams = interpretation.method.getParameters();

						params = new Object[mParams.length];

						// set first parameters
						for (int i = 0; i < mParams.length - 1; i++) {
							if (i < resultsBuffer.size())
								params[i] = resultsBuffer.get(i);
						}

						// set last parameter
						if (params.length > 0) {
							Parameter lastParam = mParams[mParams.length - 1];
							if (lastParam.isVarArgs()) {
								int start = mParams.length - 1;
								Class<?> varArgsType = lastParam.getType().getComponentType();
								Object varArgs = Array.newInstance(varArgsType, resultsBuffer.size() - start);

								for (int i = start; i < resultsBuffer.size(); i++) {
									Array.set(varArgs, i - start, varArgsType.cast(resultsBuffer.get(i)));
								}
								params[params.length - 1] = varArgs;
							} else {
								if (params.length == resultsBuffer.size())
									params[params.length - 1] = resultsBuffer.get(params.length - 1);
								else if (params.length < resultsBuffer.size() && !noBufferClear)
									System.err.println("**WARNING** [SemanticAnalyzer] some semantic result "
											+ "might be lost for the method '" + interpretation.method + "'");

							}
						}

						// invoke
						result = interpretation.invoke(params);

						if (!noBufferClear)
							resultsBuffer.clear();

						if (result != null)
							resultsBuffer.add(result);
						break;
					}
				}
			}

			// if(!executed) { interpreter.nonTerminal(tree, resultsBuffer);return;}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof ClassCastException)
				System.err.println("The method\n\t'" + current.method
						+ "'\nwas called with the following parameters\n\t" + Arrays.deepToString(params));
			throw new SemanticException(e.getCause(), tree, current.interpreter.getClass().getCanonicalName() + "."
					+ current.method.getName() + " " + Arrays.toString(current.method.getParameters()));
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Implements the iterative algorithm that visits the specified syntax tree and
	 * calls the semantic actions on the interpreter
	 * 
	 * @param tree
	 *            the syntax tree root to analyze
	 * @return the result of semantic analysis or null
	 * @throws SemanticException
	 *             if the interpreter discovers semantic errors
	 * @throws AxiomBufferClearException
	 *             if more than one result are returned by the axiom semantic action
	 */
	public Object analyze(SyntaxTree tree) throws SemanticException {

		// data structures
		LinkedList<TreeNode> stack = new LinkedList<>();
		Object result;

		// put the root in the stack
		TreeNode root = new TreeNode(tree, null);
		stack.push(root);

		while (stack.size() > 0) {
			TreeNode node = stack.peek();

			if (node.tree.terminal()) {
				
				result = invokeTerminalMethod(node.tree);

				if (result == ControlCode.TERMINATE)
					return null;

				if (result != null && node.father != null)
					node.father.results.add(result);

				stack.pop();
			} else {
				if (node.expanded) {

					invokeNonTerminalMethod(node.tree, node.results);

					if (node.results.size() > 0 && node.results.get(0) == ControlCode.TERMINATE)
						return null;

					if (node.father != null)
						node.father.results.addAll(node.results);
					stack.pop();
				} else {
					SyntaxIterator it = node.tree.iteratorFromLast();
					while (it.hasPrevious())
						stack.push(new TreeNode(it.previous(), node));
					node.expanded = true;
				}
			}
		}

		if (root.results.size() == 0)
			return null;

		return root.results.remove(0);
	}

}
