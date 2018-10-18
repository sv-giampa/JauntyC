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

import jointyc.analysis.StandardCompiler;
import jointyc.analysis.lexer.StandardLexer;
import jointyc.analysis.parser.EditableParser;
import jointyc.analysis.parser.StandardParser;
import jointyc.analysis.parser.exception.InfiniteRecursionException;
import jointyc.analysis.parser.exception.InvalidRuleNameException;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;
import jointyc.analysis.semantic.Interpreter;
import jointyc.analysis.semantic.exception.SemanticException;
import jointyc.charsequence.FileCharSequence;


/**
 * Define the JointyC Definition Language Compiler. This class is used to compile
 * the files that specify the lexicon and the grammar of a language using the JointyC Definition Language.
 * 
 * @author Salvatore Giampà
 **/
public class JdlCompiler {
	private final JdlInterpreter interpreter;
	final StandardCompiler compiler;
	
	/**
	 * Construct a new JDL Compiler
	 **/
	public JdlCompiler(){
		interpreter = new JdlInterpreter(this);
		compiler = createCompiler();
	}
	
	//creates the lexer, the parser and the compiler for the JointyC Definition Language
	private StandardCompiler createCompiler(){ //hard-coded compiler
		StandardLexer jdlLexer = new StandardLexer();
		StandardParser jdlParser = new StandardParser();
		
		//general lexicon
		jdlLexer.addType("language", "language","\"language\" string");
		jdlLexer.addType("languageName", "[a-zA-Z0-9_]+", "language name");
		jdlLexer.addType("langId", "[a-zA-Z0-9_]+", "language identifier");
		jdlLexer.addType("name", "[a-zA-Z0-9\\.\\-\\+\\*]+", "A name containing letters, digits and some special characters (\".\",\"-\",\"+\",\"*\")");
		jdlLexer.addType("lex", "lexicon", "\"lexicon\" string");
		jdlLexer.addType("grammar", "grammar", "\"grammar\" string");
		jdlLexer.addType("blockOpen", "\\{", "{");
		jdlLexer.addType("blockClose", "\\}", "}");
		jdlLexer.addType("labelSeparator", "\\:", ":");
		jdlLexer.addType("separator", "\\;", ";");
		jdlLexer.addType("import", "import", "import");
		jdlLexer.addType("importing", "\\\"[a-zA-Z0-9\\_\\.\\/\\\\]+\\\"", "file name to import");
		
		//lexer lexicon
		jdlLexer.addType("operandSeparator", ",", ",");
		jdlLexer.addType("type", "[a-zA-z][a-zA-Z0-9]*", "type name");
		jdlLexer.addType("skip", "\\!", "!");
		jdlLexer.addType("typeAssign", "=", "=");
		jdlLexer.addType("regex", "(?s)(/.*?/\\$)", "regular expression without space characters");
		jdlLexer.addType("description", "\\\".*\\\"", "description");
		jdlLexer.addType("typeSep", "\\;", ";");
		
		jdlLexer.addType("compositeOpen", "\\<", "<");
		jdlLexer.addType("compositeClose", "\\>", ">");
		jdlLexer.addType("and", "\\&", "&");
		jdlLexer.addType("or", "\\|", "|");

		//parser lexicon
		jdlLexer.addType("ruleName", "[a-zA-Z][a-zA-Z0-9_]*", "rule name");
		jdlLexer.addType("productionSymbol", "=", "=");
		
		jdlLexer.addType("idSep", "\\.");
		jdlLexer.addType("terminalPrefix", EditableParser.TERMINAL_PREFIX_REGEX, "terminal prefix");
		jdlLexer.addType("ruleProduct", "[a-zA-Z][a-zA-Z0-9_]*", "rule product");
		jdlLexer.addType("emptyString", "\\#", "#");
		jdlLexer.addType("ruleSeparator", "\\;", ";");
		jdlLexer.addType("productionSeparator", "\\|", "|");
		
		jdlLexer.addType("commentText", "(?s)(/\\*.*?\\*/|//[^\\n]*)", "comment", true);
		jdlLexer.addType("nonSkippable", "[^\\s]", "not ignorable character");
		
		try {
			jdlParser.addRule("language", "$language", "$labelSeparator", "$languageName", "$separator", "compiler");
			jdlParser.addRule("compiler", "importList", "lex", "grammar"); //
			
			//import
			jdlParser.addRule("importList", "import", "$separator", "importList");
			jdlParser.addRule("importList");
			jdlParser.addRule("import", "$import", "$importing");
			
			//lexer rules (token types)
			jdlParser.addRule("lex", "$lex", "$labelSeparator", "$blockOpen", "lexRuleList", "$blockClose");
			jdlParser.addRule("lex");
			jdlParser.addRule("lexRuleList", "lexRule", "$separator", "lexRuleListEps");
			jdlParser.addRule("lexRuleListEps", "lexRuleList");
			jdlParser.addRule("lexRuleListEps");
			jdlParser.addRule("lexRule", "skip", "$type", "$typeAssign", "typeValue", "description");

			//type composing rules
			jdlParser.addRule("typeValue", "$regex");
			jdlParser.addRule("typeValue", "compositeType");

			jdlParser.addRule("compositeType", "$compositeOpen", "compositeElem", "compositeTail");
			jdlParser.addRule("compositeTail", "$and", "compositeElem", "compositeTail");
			jdlParser.addRule("compositeTail", "$or", "compositeElem", "compositeTail");
			jdlParser.addRule("compositeTail", "$compositeClose");

			jdlParser.addRule("compositeElem", "$type");
			jdlParser.addRule("compositeElem", "$regex");
			
			
			jdlParser.addRule("lex_rule_eps", "lexRule");
			jdlParser.addRule("lex_rule_eps");
			jdlParser.addRule("skip", "$skip");
			jdlParser.addRule("skip");
			jdlParser.addRule("description", "$operandSeparator", "$description");
			jdlParser.addRule("description");
			
			//parser grammar
			jdlParser.addRule("grammar", "$grammar", "$labelSeparator", "$blockOpen", "ruleList", "$blockClose");
			jdlParser.addRule("ruleList", "rule", "$separator", "ruleListEps");
			jdlParser.addRule("ruleListEps", "ruleList");
			jdlParser.addRule("ruleListEps");
			
			jdlParser.addRule("rule", "ruleName", "$productionSymbol", "product_list");
			jdlParser.addRule("ruleName", "$langId", "$idSep", "$ruleName");
			jdlParser.addRule("ruleName", "$ruleName");
			
			jdlParser.addRule("product_list", "ruleProduct", "product_list_eps");
			jdlParser.addRule("product_list", "$emptyString");
			jdlParser.addRule("product_list_eps", "ruleProduct", "product_list_eps");
			jdlParser.addRule("product_list_eps", "$productionSeparator", "product_list");
			jdlParser.addRule("product_list_eps");
			
			jdlParser.addRule("ruleProduct", "terminalPrefix", "$langId", "$idSep", "$ruleProduct");
			jdlParser.addRule("ruleProduct", "terminalPrefix", "$ruleProduct");

			jdlParser.addRule("terminalPrefix", "$terminalPrefix");
			jdlParser.addRule("terminalPrefix");
			
		} 
		
		// grammar hard-coding errors
		catch (InfiniteRecursionException e) {
			e.printStackTrace();
		} catch (InvalidRuleNameException e) {
			e.printStackTrace();
		}
		
		jdlParser.setLexer(jdlLexer);
		
		return new StandardCompiler(jdlParser, interpreter);
	}
	
	/**
	 * Compile a JDL string and creates a StandardCompiler with the specified interpreter
	 * @param source the JDL source string
	 * @param interpreter the interpreter of the language
	 * @return a StandardCompiler for the language
	 * @throws UnexpectedSymbolException if the source string presents syntax errors
	 * @throws SemanticException if semantic errors are discovered
	 */
	public StandardCompiler compileSource(CharSequence source, Interpreter interpreter) throws UnexpectedSymbolException, SemanticException{
		this.interpreter.interpreterClass = interpreter.getClass();
		EditableParser parser = (EditableParser) compiler.compile(source);
		this.interpreter.reset();
		return new StandardCompiler(parser, interpreter);
	}
	
	/**
	 * Compile a JDL text file and creates a StandardCompiler with the specified interpreter
	 * @param file the path of the JDL file
	 * @param interpreter the interpreter of the language
	 * @return a StandardCompiler for the language
	 * @throws IOException if an IO/error occurs in file reading
	 * @throws UnexpectedSymbolException if the source string presents syntax errors
	 * @throws SemanticException if semantic errors are discovered
	 */
	public StandardCompiler compileFile(String file, Interpreter interpreter) throws IOException, UnexpectedSymbolException, SemanticException{
		FileCharSequence source = new FileCharSequence(new File(file));
		return compileSource(source, interpreter);
	}

	/**
	 * Compile a JDL text stream and creates a StandardCompiler with the specified interpreter
	 * @param in the stream to compile
	 * @param interpreter the interpreter of the language
	 * @return a StandardCompiler for the language
	 * @throws IOException if an IO/error occurs
	 * @throws UnexpectedSymbolException if the source string presents syntax errors
	 * @throws SemanticException if semantic errors are discovered
	 */
	public StandardCompiler compileStream(InputStream in, Interpreter interpreter) throws IOException, UnexpectedSymbolException, SemanticException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder source = new StringBuilder();
		String line;
		while((line = reader.readLine())!=null)
			source.append(line).append('\n');
		in.close();

		return compileSource(source.toString(), interpreter);
	}

	/**
	 * Compile a JDL text file included as a Java resource and creates a StandardCompiler with the specified interpreter
	 * @param path the path of the resource to compile.<br>
	 * 			Examples: <ul>
	 * 				<li>absolute package - "my/java/package/resource.jdl": the file "resource.jdl" is in the package "my.java.package";
	 * 				<li>relative package - "resource.jdl": the file "resource.jdl" is in the same package of the interpreter
	 * 			</ul>
	 * @param interpreter the interpreter of the language
	 * @return a StandardCompiler for the language
	 * @throws IOException if an IO/error occurs in file reading
	 * @throws UnexpectedSymbolException if the source string presents syntax errors
	 * @throws SemanticException if semantic errors are discovered
	 */
	public StandardCompiler compileResource(String path, Interpreter interpreter) throws IOException, UnexpectedSymbolException, SemanticException{
		InputStream in = interpreter.getClass().getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder source = new StringBuilder();
		String line;
		while((line = reader.readLine())!=null)
			source.append(line).append('\n');
		in.close();

		return compileSource(source.toString(), interpreter);
	}
}
