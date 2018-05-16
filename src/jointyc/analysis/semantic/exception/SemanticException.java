/**
 *  Copyright 2017 Salvatore Giamp�
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

package jointyc.analysis.semantic.exception;

import jointyc.analysis.parser.SyntaxTree;

/**
 * Usually thrown when an interpretation error occurs (For example, considering some strongly typed language, the type checking reveals an error)
 * @author Salvatore Giamp�
 *
 */
public class SemanticException extends Exception {
	private static final long serialVersionUID = 1021579297957230596L;
	
	public final Object supplement;
	public final String token, source, tag;
	public int start, end, startLine, startColumn, endLine, endColumn;
	
	/**
	 * Creates a new SemanticException
	 * @param message a string message printed with the stack trace
	 * @param supplement a supplementary object, containing more specific information about the error
	 * @param tree the syntax tree node in which the error occurs
	 * @param tag a tag associated with the exception, usually the name of compiler class, the name of the compiled language or
	 * 				the name of the compiled module. When the language supports importing/including directives for multi-module programming,
	 * 				the tag is useful to address the file which contains the error.
	 */
	public SemanticException(String message, Object supplement, SyntaxTree tree, String tag){ //String token, int start, int end, String input){
		super(message);
		this.tag = tag;
		this.supplement = supplement;
		this.token = tree.token();
		this.start = tree.start();
		this.end = tree.end();
		this.source = tree.source();
		
		startLine = 1; startColumn = 1; endLine = 1; endColumn = 1;
		
		for(int i=0; i<end; i++){
			if(i<start){
				if(source.charAt(i) == '\n'){
					startColumn=1; startLine++;
					endColumn=1; endLine++;
				}
				else{
					startColumn++;
					endColumn++;
				}
			}
			else{
				if(source.charAt(i) == '\n'){
					endColumn=1; endLine++;
				}
				else endColumn++;
			}
		}
	}
	
	public SemanticException(String message, SyntaxTree tree){ //String token, int start, int end, String input){
		this(message,null,tree,null);
	}
	
	public SemanticException(String message, SyntaxTree tree, String tag){ //String token, int start, int end, String input){
		this(message,null,tree,tag);
	}
	
	public SemanticException(String message, Object supplement, SyntaxTree tree){ //String token, int start, int end, String input){
		this(message,supplement,tree,null);
	}
	
	public SemanticException(Exception supplement, SyntaxTree tree){ //String token, int start, int end, String input){
		this(supplement.getMessage(), supplement, tree);
	}
	
	
	public SemanticException(Exception supplement, SyntaxTree tree, String tag){ //String token, int start, int end, String input){
		this(supplement.getMessage(), supplement, tree, tag);
	}
	
	@Override
	public String toString() {
		return  String.format("[line: %s; column: %s; position: %s] %s\n\n%s ", startLine, startColumn, start, tag!=null?tag + " ":"", supplement!=null?supplement:"") + super.toString();                   
	}
	
}
