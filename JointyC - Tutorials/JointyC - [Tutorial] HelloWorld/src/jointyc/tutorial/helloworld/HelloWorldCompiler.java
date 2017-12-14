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

import java.io.IOException;

import jointyc.analysis.StandardCompiler;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;
import jointyc.analysis.semantic.exception.SemanticException;
import jointyc.jdlc.JdlCompiler;

public class HelloWorldCompiler {
	
	private StandardCompiler compiler;
	
	public HelloWorldCompiler() {
		JdlCompiler jdl = new JdlCompiler();
		
		try {
			compiler = jdl.compileResource("HelloWorld.jdl", new HelloWorldInterpreter());
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnexpectedSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SemanticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(-1);
	}
	
	public void compile(String source) throws UnexpectedSymbolException, SemanticException {
		compiler.compile(source);
	}
	
	public static void main(String[] args) {
		HelloWorldCompiler compiler = new HelloWorldCompiler();
		
		try {
			System.out.println("test #1 - \"hello    world   !\"");
			System.out.print("greeting: ");
			compiler.compile("hello world   !");
			
			System.out.println();
			System.out.println("test #2 - \"hello world\"");
			System.out.print("greeting: ");
			compiler.compile("hello world");
			
			System.out.println();
			System.out.println("test #3 - \"\"");
			System.out.print("greeting: ");
			compiler.compile("");
			
			System.out.println();
			System.out.println("test #4 - \"    wow\"");
			System.out.print("greeting: ");
			compiler.compile("    wow");		//must throw an UnexpectedSymbolException
		} catch (UnexpectedSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SemanticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
