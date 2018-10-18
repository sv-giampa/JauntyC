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

package jointyc.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.PatternSyntaxException;

import jointyc.analysis.lexer.Lexer;
import jointyc.analysis.parser.Parser;
import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;
import jointyc.analysis.semantic.Interpreter;
import jointyc.analysis.semantic.SemanticAnalyzer;
import jointyc.analysis.semantic.exception.SemanticException;
import jointyc.charsequence.FileCharSequence;

/**
 * Defines a standard implementation of a compiler, assembling the given parser and the given interpreter.
 * This compiler uses the standard syntax tree exploring machinery specified by {@link SemanticAnalyzer} class.
 * 
 * @author Salvatore Giampà
 *
 */
public final class StandardCompiler implements Serializable {
	private static final long serialVersionUID = 7410382528782276215L;
	
	private Parser parser;
	private Interpreter interpreter;
	private SemanticAnalyzer analyzer;
	
	public StandardCompiler(Parser parser, Interpreter interpreter){
		if(parser == null)
			throw new NullPointerException("null parser");
		if(interpreter == null)
			throw new NullPointerException("null interpreter");
		
		this.parser = parser;
		this.interpreter = interpreter;
		this.analyzer = new SemanticAnalyzer(interpreter, parser);
	}
	
	public Object compile(CharSequence source) throws UnexpectedSymbolException, SemanticException{
		parser.getLexer().setInput(source);
		SyntaxTree tree = parser.parse();
		return analyzer.analyze(tree);
	}
	
	public Object compile(File file) throws UnexpectedSymbolException, SemanticException, IOException{
		parser.getLexer().setInput(new FileCharSequence(file));
		SyntaxTree tree = parser.parse();
		return analyzer.analyze(tree);
	}

	/**
	 * Get the lexer used by this compiler
	 * @return a {@link Lexer} object
	 */
	public Lexer getLexer(){return parser.getLexer();}

	/**
	 * Get the parser used by this compiler
	 * @return a {@link Parser} object
	 */
	public Parser getParser(){return parser;}

	/**
	 * Get the interpreter used by this compiler
	 * @return an {@link Interpreter} object
	 */
	public Interpreter getInterpreter(){return interpreter;}

}
