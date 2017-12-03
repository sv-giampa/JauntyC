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

import java.io.IOException;

import jauntyc.analysis.StandardCompiler;
import jauntyc.analysis.parser.exception.UnexpectedSymbolException;
import jauntyc.analysis.semantic.exception.SemanticException;
import jauntyc.jdlc.JdlCompiler;

public class TemplateCompiler {
	
	//declare the standard compiler for the language
	private StandardCompiler compiler;
	
	
	public TemplateCompiler() {
		/*
		 * At construction time, compile the grammar of the language
		 * by using a JdlCompiler instance
		 */
		JdlCompiler jdlc = new JdlCompiler();
		
		try {
			/*
			 * compile the StandardCompiler specifying the *.jdl file and the interpreter 
			 */
			compiler = jdlc.compileResource("Template.jdl", new TemplateInterpreter());
			
			/*
			 * also the following alternative methods can be used:
			 * 		
			 * 	search the *.jdl file in the working directory or in another absolute or relative path
			 * 		compiler = jdlc.compileFile("./Template.jdl", new TemplateInterpreter());
			 * 
			 * 	read the JDL text source string from an input stream (e.g. FileInputStream, socket input stream, etc...)
			 * 		compiler = jdlc.compileStream(inputStream, new TemplateInterpreter());
			 * 
			 * 	put the JDL source string in a standard String (for very very little grammars}
			 * 		compiler = jdlc.compileSource("language: template; lexicon:{...} grammar:{...}", new TemplateInterpreter());
			 */
			
			/*
			 * Then forget the JdlCompiler ;-D
			 */
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnexpectedSymbolException e) {
			e.printStackTrace();
		} catch (SemanticException e) {
			e.printStackTrace();
		}
		System.exit(-1);
	}
	
	/**
	 * Compile a template string
	 * @param source the template source string
	 * @return the string "template"
	 * @throws UnexpectedSymbolException if the source string does not respect the Template grammar
	 * @throws SemanticException usually this exception must be caught in this method and replaced with
	 * 				the proper application exceptions
	 */
	public String compile(String source) throws UnexpectedSymbolException, SemanticException {
		/*
		 * use the StandardCompiler to obtain the representation of
		 * the source string, then simply return it
		 */
		try {
			return (String) compiler.compile(source);
		} catch (SemanticException e) {
			/*
			 * here, you must analyze the SemanticException and throw
			 * your own exceptions basing on the supplementary information
			 * in the caught SemanticException
			 */
			
			throw e; //stub throw
		}
	}
	
	/**
	 * Run this to see the order of printlns in the TemplateInterpreter and
	 * to see how the StandardParser works (see the code).
	 * @param args
	 */
	public static void main(String[] args) {
		TemplateCompiler templateCompiler = new TemplateCompiler();
		
		try {
			String result;
			String input;
			
			//syntactically correct
			input = "template";
			System.out.println("compilig the following source string: \'" + input + "\'");
			result = templateCompiler.compile(input);
			System.out.println("- result: " + result);

			//syntactically correct
			input = " aaa atemplate aaa";
			System.out.println("compilig the following source string: \'" + input + "\'");
			result = templateCompiler.compile(input);
			System.out.println("- result: " + result);

			//syntactically INCORRECT
			input = "qr template";
			System.out.println("compilig the following source string: \'" + input + "\'");
			result = templateCompiler.compile(input); //must throw an UnexpectedSymbolException
		} catch (UnexpectedSymbolException e) {
			//tell to the user what went wrong
		} catch (SemanticException e) {
			/*
			 * manage specific semantic errors, catch the
			 * application logic exceptions
			 * (see the TemplateCompiler.compile() method description)
			 */
		}
	}
	
}
