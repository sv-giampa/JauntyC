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
import jointyc.analysis.parser.Parser;
import jointyc.analysis.parser.StandardParser;
import jointyc.analysis.parser.SyntaxIterator;
import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.parser.exception.InfiniteRecursionException;
import jointyc.analysis.parser.exception.InvalidRuleNameException;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;
import jointyc.analysis.semantic.Interpreter;
import jointyc.analysis.semantic.SemanticAnalyzer;
import jointyc.analysis.semantic.annotation.NonTerminalToken;
import jointyc.analysis.semantic.annotation.TerminalToken;
import jointyc.analysis.semantic.exception.SemanticException;
import jointyc.jdlc.exception.NotDeclaredTerminalException;

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
	
	public void reset() {
		imports.clear();
	}

	@TerminalToken(value="languageName")
	private Object languageName(SyntaxTree tree) {
		String lang = tree.token().toString();
		languageName = lang;
		assiome = null;
		
		//if this language has been imported, terminate the semantic analysis
		if(imports.contains(lang))
			return SemanticAnalyzer.ControlCode.TERMINATE;
		
		imports.add(lang);
		return lang;
	}

	@TerminalToken(value="langId")
	@TerminalToken(value="ruleName")
	@TerminalToken(value="ruleProduct")
	private String getToken(SyntaxTree tree) {
		return tree.token().toString();
	}

	@TerminalToken(value="type")
	private String type(SyntaxTree tree) {
		return languageName + "." + tree.token();
	}
	
	@TerminalToken(value="importing")
	private String importing(SyntaxTree tree) {
		String filename = tree.token().toString();
		return filename.substring(1, filename.length()-1);
	}

	@TerminalToken(value="productionSeparator")
	private String productionSeparator() {
		return "productionSeparator";
	}
	
	@TerminalToken(value="emptyString")
	private String emptyString() {
		return "emptyString";
	}
	
	@TerminalToken(value="terminalPrefix")
	private String terminalPrefix() {
		return "terminalPrefix";
	}
	
	@TerminalToken(value="regex")
	private String regex(SyntaxTree tree) {
		String regex = tree.token().toString();
		return regex.substring(1,regex.length()-2);
	}
	
	@TerminalToken(value="description")
	private String description(SyntaxTree tree) {
		String description = tree.token().toString();
		return "'" + description.substring(1,description.length()-1) + "'";
	}
	
	@NonTerminalToken(ruleHead="language")
	private Parser language() {
		parser.setAxiom(assiome);
		parser.setLexer(lexer);
		return parser;
	}
	
	//analyze the importing file, using this same interpreter object, by compiling the importing file with
	//the JdlCompiler which is using this interpreter
	@NonTerminalToken(ruleHead="import")
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
	
	@NonTerminalToken(ruleHead="lexRule")
	private void lexRule(boolean skip, String type, String regex, String description) {
		lexer.addType(type, regex, description, skip);
		if(DEBUG) System.out.printf("%s%s = /%s/ , \"%s\"\n", skip?"!":"", type, regex, description);
	}
	
	
	@NonTerminalToken(ruleHead="rule")
	private void rule(String head, String... body) throws InvalidRuleNameException, InfiniteRecursionException {

		if(assiome == null)
			assiome = head;
		
		LinkedList<String> production = new LinkedList<>();
		for(String product : body) {
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
	}
	
	@NonTerminalToken(ruleHead="ruleName", ruleProduction= {"$langId"})
	private String ruleName(String lang, String ruleName) {
		return lang + "." + ruleName;
	}
	
	@NonTerminalToken(ruleHead="ruleName", ruleProduction= {"$ruleName"})
	private String ruleName2(String ruleName) {
		return languageName + "." + ruleName;
	}
	
	@NonTerminalToken(ruleHead="ruleProduct", ruleProduction= {"terminalPrefix", "$langId", "$idSep", "$ruleProduct"})
	private String ruleProduct(String...resultsBuffer) {
		boolean terminal = resultsBuffer.length == 3;
		int start = 0;
		if(terminal) start = 1;
		String lang = (String) resultsBuffer[start];
		String product = (String) resultsBuffer[start+1];
		if(terminal) {
			String terminalName = lang + "." + product;
			if(lexer.regex(terminalName) == null) 
				throw new NotDeclaredTerminalException(terminalName);
			product = EditableParser.TERMINAL_PREFIX + terminalName;		
		}
		else
			product = lang + "." + product;
		return product;
	}
	
	@NonTerminalToken(ruleHead="ruleProduct", ruleProduction= {"terminalPrefix", "$ruleProduct"})
	private String ruleProduct2(String...resultsBuffer) {
		boolean terminal = resultsBuffer.length == 2;
		int start = 0;
		if(terminal) start = 1;
		String product = languageName + "." + (String) resultsBuffer[start];
		if(terminal) {
			if(lexer.regex(product) == null) 
				throw new NotDeclaredTerminalException(product);
			product = EditableParser.TERMINAL_PREFIX + product;
		}
		return product;
	}

	//if the skip predicate is not empty, the current token type must be skipped
	@NonTerminalToken(ruleHead="skip", ruleProduction= {"!#"})
	private Boolean skip() {
		return Boolean.TRUE;
	}
	
	//if the skip predicate is empty, the current token type must not be skipped
	@NonTerminalToken(ruleHead="skip", ruleProduction= {"#"})
	private Boolean dontSkip() {
		return Boolean.FALSE;
	}

	@NonTerminalToken(ruleHead="compositeElem", ruleProduction= {"$type"})
	private String compositeElem(String type) {
		String regex = lexer.regex(type);
		if(regex == null) {
			throw new RuntimeException("regex is null for the type '" + type + "'");
		}
		return regex;
	}
	
	@NonTerminalToken(ruleHead="compositeTail", ruleProduction= {"$compositeClose"})
	private String compositeTail() {
		return "";
	}
	
	@NonTerminalToken(ruleHead="compositeTail", ruleProduction= {"$or"})
	private String compositeTailOr(String regex, String tail) {
		return "|" + regex + tail;
	}
	
	@NonTerminalToken(ruleHead="compositeTail", ruleProduction= {"$and"})
	private String compositeTailAnd(String regex, String tail) {
		return regex + tail;
	}
	
	@NonTerminalToken(ruleHead="compositeType")
	private String compositeType(String regex, String tail) {
		return regex + tail;
	}

}
