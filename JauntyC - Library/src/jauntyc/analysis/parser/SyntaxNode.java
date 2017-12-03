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

package jauntyc.analysis.parser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jauntyc.analysis.lexer.Lexer;

/**
 * Internal implementation of a {@link SyntaxTree} for the {@link StandardParser}
 * @author Salvatore Giampà
 *
 */
final class SyntaxNode implements SyntaxTree{
	boolean terminal;
	String type;
	int start;
	int end;
	Set<String> similarTypes;
	String source;
	Lexer lexer;
	
	LinkedList<SyntaxNode> nexts;
	
	void addNext(SyntaxNode next){
		nexts.addFirst(next);
	}
	
	public SyntaxNode(Lexer lexer){
		nexts = new LinkedList<>();
		this.source = lexer.input();
		this.lexer = lexer;
	}
	
	public List<SyntaxNode> getNexts(){
		return Collections.unmodifiableList(nexts);
	}
	

	@Override
	public SyntaxIterator iterator() {
		return new SyntaxIterator(nexts, false);
	}
	
	private String toString(int level){
		StringBuilder sb = new StringBuilder();
		sb.append("|\n|\n");
		for(int i = 0; i<=level; i++){
			if(/*i==level ||*/ i==0)
				sb.append("|________");
			else
				sb.append("|________");
		}
		sb.append("> ");
		
		sb.append(String.format("token=\"%s\", type=%s, similarTypes=%s, [start,end]=[%d,%d], terminal=%s", token().replaceAll("\\n", "\\\\n"), type, similarTypes, start, end, terminal));
		sb.append('\n');
		
		for(SyntaxTree subtree : this){
			sb.append(((SyntaxNode) subtree).toString(level+1));
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return token()+"\n"+toString(0);
	}

	@Override
	public boolean terminal() {
		return terminal;
	}

	@Override
	public String type() {
		return type;
	}

	@Override
	public int nexts() {
		return nexts.size();
	}

	@Override
	public int start() {
		return start;
	}

	@Override
	public int end() {
		return end;
	}

	@Override
	public String token() {
		if(start <= end)
			return source.substring(start, end+1);
		else
			return "";
	}

	@Override
	public Set<String> similarTypes() {
		return Collections.unmodifiableSet(similarTypes);
	}

	@Override
	public String source() {
		return source;
	}

	@Override
	public Lexer lexer() {
		return lexer;
	}

	@Override
	public SyntaxIterator iteratorFromLast() {
		return new SyntaxIterator(nexts, true);
	}
}
