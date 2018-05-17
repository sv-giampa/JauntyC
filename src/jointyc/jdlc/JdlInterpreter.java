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

package jointyc.jdlc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import jointyc.analysis.lexer.EditableLexer;
import jointyc.analysis.lexer.StandardLexer;
import jointyc.analysis.parser.EditableParser;
import jointyc.analysis.parser.StandardParser;
import jointyc.analysis.parser.SyntaxIterator;
import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.parser.exception.InfiniteRecursionException;
import jointyc.analysis.parser.exception.InvalidRuleNameException;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;
import jointyc.analysis.semantic.Interpreter;
import jointyc.analysis.semantic.SemanticAnalyzer;
import jointyc.analysis.semantic.exception.SemanticException;

/**
 * Defines the interpreter for the JointyC Definition Language
 * @author Salvatore Giampà
 *
 */
class JdlInterpreter implements Interpreter {
	
	//Enable this to view standard I/O prints
	private static final boolean DEBUG = false;
	
	//class of the interpreter of the language that the JdlCompiler is compiling.
	//used to search for imported languages that are in the same directory of the interpreter
	Class<?> interpreterClass = this.getClass();
	
	//objects to build
	private EditableParser parser;
	private EditableLexer lexer;
	
	//list of imported language modules
	private LinkedList<String> imports = new LinkedList<>();
	
	//name of the current (sub)language
	private String languageName = null;
	
	//The compiler that is using this interpreter instance
	private JdlCompiler compiler;
	
	//the axiom of the built parser
	private String assiome;

	public JdlInterpreter(JdlCompiler compiler) {
		this.compiler = compiler;
		this.parser = new StandardParser();
		this.lexer = new StandardLexer();
	}
	
	@Override
	public Object terminal(SyntaxTree tree) throws SemanticException {
		switch(tree.type()) {
		case "languageName":
			String lang = tree.token();
			languageName = lang;
			assiome = null;
			
			//if this language has been imported, terminate the semantic analysis
			if(imports.contains(lang))
				return SemanticAnalyzer.ControlCode.TERMINATE;
			
			imports.add(lang);
			return lang;
		case "langId":
			return tree.token();
		case "ruleName":
			return tree.token();
		case "ruleProduct":
			return tree.token();
		case "type":
			return languageName + "." + tree.token();
		case "importing":
			String filename = tree.token();
			return filename.substring(1, filename.length()-1);
		case "productionSeparator":
			return "productionSeparator";
		case "emptyString":
			return "emptyString";
		case "regex":
			String regex = tree.token();
			return regex.substring(1,regex.length()-2);
		case "description":
			String description = tree.token();
			return "'" + description.substring(1,description.length()-1) + "'";
		case "terminalPrefix":
			return "terminalPrefix";
		}
		return null;
	}
	
	@Override
	public void nonTerminal(SyntaxTree tree, List<Object> resultsBuffer) throws SemanticException {
		if(tree.query("language")) {
			resultsBuffer.clear();
			parser.setAxiom(assiome);
			parser.setLexer(lexer);
			resultsBuffer.add(parser);
			return;
		}
		else if(tree.query("import")) {
			
			String filename = (String) resultsBuffer.get(0);
			
			try {
				importLanguage(filename);
				resultsBuffer.clear();
			} catch (IOException e) {
				throw new SemanticException(e, tree, "JDL Compiler - language: " + imports.peek());
			} catch (UnexpectedSymbolException e) {
				throw new SemanticException(e, tree, "JDL Compiler - language: " + imports.peek() + ". Error in imported file \"" + filename + "\"");
			}
			return;
		}
		else if(tree.query("lexRule")) {
			boolean skip = (Boolean) resultsBuffer.get(0);
			String type = (String) resultsBuffer.get(1);
			String regex = (String) resultsBuffer.get(2);
			String description = null;
			
			if(resultsBuffer.size() > 3)
				description = (String) resultsBuffer.get(3);
			
			lexer.addType(type, regex, description, skip);
			
			if(DEBUG) System.out.printf("%s%s = /%s/ , \"%s\"\n", skip?"!":"", type, regex, description);
			
			resultsBuffer.clear();
			return;
		}
		else if(tree.query("rule")) {
			String head = (String) resultsBuffer.remove(0);
			
			if(assiome == null)
				assiome = head;
			
			LinkedList<String> production = new LinkedList<>();
			
			try {
				for(Object o : resultsBuffer) {
					String product = (String) o;
					if(product.equals("emptyString")) {
						break;
					}
					if(product.equals("productionSeparator")) {
						parser.addRule(head, production);
						if(DEBUG) System.out.printf("%s -> %s\n", head, production);
						production = new LinkedList<>();
					}
					else 
						production.add(product);
				}
				parser.addRule(head, production);
				if(DEBUG) System.out.printf("%s -> %s\n", head, production);
			}catch(InvalidRuleNameException e) {
				throw new SemanticException(e, tree.iterator().next(), "JDL Compiler - language: " + imports.peek() + "");
			} catch (InfiniteRecursionException e) {
				SyntaxIterator it = tree.iterator();
				it.next();
				throw new SemanticException(e, it.next(), "JDL Compiler - language: " + imports.peek() + "");
			}
			
			resultsBuffer.clear();
			return;
		}
		else if(tree.query("ruleName", "$langId")) {
			String lang = (String) resultsBuffer.get(0);
			String ruleName = (String) resultsBuffer.get(1);
			ruleName = lang + "." + ruleName;
			resultsBuffer.clear();
			resultsBuffer.add(ruleName);
			return;
		}
		else if(tree.query("ruleName", "$ruleName")) {
			String ruleName = (String) resultsBuffer.get(0);
			ruleName = languageName + "." + ruleName;
			resultsBuffer.clear();
			resultsBuffer.add(ruleName);
			return;
		}
		else if(tree.query("ruleProduct", "terminalPrefix", "$langId", "$idSep", "$ruleProduct")) {
			boolean terminal = resultsBuffer.size() == 3;
			if(terminal) resultsBuffer.remove(0);
			String lang = (String) resultsBuffer.get(0);
			String product = (String) resultsBuffer.get(1);
			if(terminal) {
				String terminalName = lang + "." + product;
				if(lexer.regex(terminalName) == null) 
					throw new SemanticException("Terminal \"" + terminalName + "\" not declared in the lexicon", tree, "JDL Compiler - language: " + imports.peek());
				product = EditableParser.TERMINAL_PREFIX + terminalName;		
			}
			else
				product = lang + "." + product;
			resultsBuffer.clear();
			resultsBuffer.add(product);
			return;
		}
		else if(tree.query("ruleProduct", "terminalPrefix", "$ruleProduct")) {
			boolean terminal = resultsBuffer.size() == 2;
			if(terminal) resultsBuffer.remove(0);
			String product = languageName + "." + (String) resultsBuffer.get(0);
			if(terminal) {
				if(lexer.regex(product) == null) 
					throw new SemanticException("Terminal \"" + product + "\" not declared in the lexicon", tree, "JDL Compiler - language: " + imports.peek());
				product = EditableParser.TERMINAL_PREFIX + product;
			}
			resultsBuffer.clear();
			resultsBuffer.add(product);
			return;
		}
		else if(tree.query("skip", "!#")) { //if the skip predicate is not empty, the current token type must be skipped
			resultsBuffer.add(Boolean.TRUE);
			return;
		}
		else if(tree.query("skip", "#")) { //if the skip predicate is empty, the current token type must not be skipped
			resultsBuffer.add(Boolean.FALSE);
			return;
		}
		else if(tree.query("compositeElem", "$type")) {
			String type = (String) resultsBuffer.remove(0);
			String regex = lexer.regex(type);
			
			if(regex == null) {
				//eccezione
			}
			resultsBuffer.add(regex);
		}
		else if(tree.query("compositeTail", "$compositeClose")) {
			resultsBuffer.add("");
		}
		else if(tree.query("compositeTail", "$or")) {
			String regex = (String) resultsBuffer.get(0);
			String tail = (String) resultsBuffer.get(1);
			
			tail = "|" + regex + tail;
			
			resultsBuffer.clear();
			resultsBuffer.add(tail);
		}
		else if(tree.query("compositeTail", "$and")) {
			String regex = (String) resultsBuffer.get(0);
			String tail = (String) resultsBuffer.get(1);
			
			tail = regex + tail;
			
			resultsBuffer.clear();
			resultsBuffer.add(tail);
		}
		else if(tree.query("compositeType")) {
			String regex = (String) resultsBuffer.get(0);
			String tail = (String) resultsBuffer.get(1);
			
			tail = regex + tail;
			
			resultsBuffer.clear();
			resultsBuffer.add(tail);
		}
	}

	public void reset() {
		imports.clear();
	}
	
	//analyze the importing file, using this same interpreter object, by compiling the importing file with
	//the JdlCompiler which is using this interpreter
	private void importLanguage(String filename) throws IOException, UnexpectedSymbolException, SemanticException {
		String source;
		InputStream input;

		input = interpreterClass.getResourceAsStream(filename);
		if(input == null)
			input = interpreterClass.getClassLoader().getResourceAsStream(filename);
		if(input != null){
			StringBuilder sb = new StringBuilder();
			BufferedReader r = new BufferedReader(new InputStreamReader(input));
			String line;
			while((line = r.readLine()) != null){
				sb.append(line);
				sb.append('\n');
			}
			source = sb.toString();
			input.close();
		}
		else
			source = new String(Files.readAllBytes(new File(filename).toPath()));
		
		String tmpLang = languageName;
		compiler.compiler.compile(source);
		languageName = tmpLang;
		assiome = null;
	}

}
