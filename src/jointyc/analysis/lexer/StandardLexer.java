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

package jointyc.analysis.lexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a basic editable lexer.
 * This lexer skips all the tokens that are not matched by a rule. So it is necessary to define a rule for every
 * relevant token. If some tokens must be recognized as erroneous, such as a special character, at least one rule must be defined to match them.
 * Usually, along with other token types of the lexicon, one another is defined to macth all the erroneous relevant characters.</br>
 * </br>
 * For instance, if only the white spaces must be skipped, it could be defined the following rule: </br><code>lexer.addType("erroneous", "[^\s]");</code>
 * </br>
 * @author Salvatore Giampà
 *
 */
public final class StandardLexer implements EditableLexer{
	
	private static class RegexEntry{
		public final String type;
		public final String regex;
		
		public RegexEntry(String type, String regex){
			this.type = type;
			this.regex = regex;
		}
	}
	
	private String input;
	private String token;
	private String tokenType;
	private int position;
	private int end;
	
	/*
	 * Not a map, but a list, so it's possible to give priority to mached types, according to the insertion order.
	 * Evaluating the use of a LinkedHashMap
	 */
	private List<RegexEntry> regexTable =  new LinkedList<>();
	
	private Map<String,String> labelTable = new HashMap<>();
	private Map<String,String> descriptionTable = new HashMap<>();
	
	private Set<String> typeSet = new HashSet<>();
	private Set<String> skippableSet = new HashSet<>();
	private Map<String, Matcher> matcherMap = new HashMap<>();
	
	public void setInput(String input){
		this.input = input;
		
		matcherMap.clear();
		position = end = 0;
		
		for(RegexEntry e : regexTable)
			matcherMap.put(e.type, Pattern.compile(e.regex).matcher(input));
	}
	
	public StandardLexer(){
	}

	@Override
	public final String token(String type){
		if(!typeSet.contains(type)) return null;
		Matcher matcher = matcherMap.get(type);
		String token = matcher.group();
		if(token != null){
			this.token = token;
			tokenType = type;
			end = matcher.end();
		}
		return token;
	}
	
	@Override
	public final String token(){
		if(token == null) return null;
		Matcher matcher = matcherMap.get(tokenType);
		end = matcher.start() + token.length();
		return token;
	}

	@Override
	public final String tokenType(){
		return tokenType;
	}

	@Override
	public final int start(){
		return position;
	}
	
	@Override
	public int end() {
		return end-1;
	}

	@Override
	public void setStart(int position){
		if(position < 0)
			throw new IllegalArgumentException("position < 0");
		if(position > input.length())
			throw new IllegalArgumentException("position > input-length");
		end = position;
	}

	@Override
	public boolean next(){
		
		int start;
		boolean skippable;
		
		do{
			typeSet.clear();
			start = Integer.MAX_VALUE;
			skippable = false;
			
			for(Entry<String, Matcher> e : matcherMap.entrySet()){
				Matcher m = e.getValue();
				if(m.find(end)){
					typeSet.add(e.getKey());
					if(m.start() < start){
						start = m.start();
					}
				}
			}
		
			Iterator<String> it = typeSet.iterator();
			while(it.hasNext()){
				String type = it.next();
				Matcher m = matcherMap.get(type);
				if(m.start() > start)
					it.remove();
			}
			
			for(String type : skippableSet)
				if(typeSet.contains(type)){
					end = matcherMap.get(type).end();
					skippable = true;
					break;
				}
			
		}while(skippable);
		
		if(typeSet.size()>0){
			for(RegexEntry e : regexTable){
				if(typeSet.contains(e.type)){
					token = matcherMap.get(e.type).group();
					tokenType = e.type;
					break;
				}
			}
			position = start;
			return true;
		}
		
		token = null;
		tokenType = null;
		position = input.length();
		end = input.length();
		
		return false;
	}

	public void addAlias(String newType, String refType){
	}
	
	public void addType(String type, String regex, String description, boolean skip) {
		regexTable.add(new RegexEntry(type, regex));
		descriptionTable.put(type, description);
		
		if(input != null)
			matcherMap.put(type, Pattern.compile(regex).matcher(input));
		
		if(skip)
			skippableSet.add(type);
		else
			skippableSet.remove(type);
	}

	@Override
	public void resetTypes() {
		regexTable.clear();
		matcherMap.clear();
		skippableSet.clear();
		descriptionTable.clear();
		labelTable.clear();
	}
	
	@Override
	public String input() {
		return input;
	}

	@Override
	public Set<String> similarTypes() {
		Set<String> set = new HashSet<>();
		
		for(String type : typeSet){
			Matcher m = matcherMap.get(type);
			if(m.start() == position && m.end()==end)
				set.add(type);
		}
		
		return set;
	}

	@Override
	public String description(String type) {
		return descriptionTable.get(type);
	}
	
	
	@Override
	protected StandardLexer clone() {
		StandardLexer lexer;
		try {
			lexer = (StandardLexer) super.clone();

			lexer.regexTable =  new LinkedList<>(regexTable);
			lexer.labelTable = new HashMap<>(labelTable);
			lexer.descriptionTable = new HashMap<>(descriptionTable);
			lexer.typeSet = new HashSet<>(typeSet);
			lexer.matcherMap = new HashMap<>(matcherMap);
			lexer.skippableSet = new HashSet<>(skippableSet);
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		return lexer;
	}

	@Override
	public String regex(String type) {
		for(RegexEntry e : regexTable)
			if(e.type.equals(type))
				return e.regex;
		return null;
	}

	/*
	public static void main(String[] args) {
		StandardLexer lexer = new StandardLexer();
		lexer.addType("NAME", "[a-z]+",  "nome");
		lexer.addType("comment", "\\<.*\\>", true);
		
		lexer.setInput("   owbc <dfg> gafs");
		
		while(lexer.next()){
			System.out.println(lexer.token() + " : " + lexer.tokenType());
		}
		
		
		lexer.next();
		
		System.out.println("default: " + lexer.token() + ", " + lexer.tokenType());
		System.out.println("NAME: " + lexer.token("NAME"));
		System.out.println("WOW: " + lexer.token("WOW"));
		System.out.println("HI: " + lexer.token("HI"));
		
		
	}*/
}
