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

package jointyc.analysis.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import jointyc.analysis.lexer.Lexer;
import jointyc.analysis.parser.exception.InfiniteRecursionException;
import jointyc.analysis.parser.exception.InvalidRuleNameException;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;
import jointyc.analysis.parser.exception.UnexpectedSymbolException.ExpectedTerminal;

/**
 * Defines a standard implementation for an editable parser.
 * This parser uses a cache to avoid revisiting of syntax structures.
 * The cache uses the LRU policy (Least Recently Used) for node replacing.
 * 
 * @author Salvatore Giampà
 *
 */
public class StandardParser implements EditableParser {

	private static final boolean DEBUG_PRINT = false;
	
	//internal representation of rule
	private static class Rule implements Iterable<String>{
		final String head;
		final List<String> production;
		private final int hashCode;
		
		private int computeHashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((head == null) ? 0 : head.hashCode());
			result = prime * result + ((production == null) ? 0 : production.hashCode());
			return result;
		}
		
		Rule(String head, List<String> production){
			this.head = head;
			this.production = Collections.unmodifiableList(new ArrayList<>(production));
			this.hashCode = computeHashCode();
		}
		
		
		@SuppressWarnings("unused")
		public String getProduct(int index) {
			return production.get(index);
		}
		
		@SuppressWarnings("unused")
		public int getProductionLength() {
			return production.size();
		}
		
		@Override
		public String toString() {
			return head + " -> " + production;
		}

		@Override
		public Iterator<String> iterator() {
			return production.iterator();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			Rule other = (Rule) obj;
			if (head == null) {
				if (other.head != null)
					return false;
			} else if (!head.equals(other.head))
				return false;
			if (production == null) {
				if (other.production != null)
					return false;
			} else if (!production.equals(other.production))
				return false;
			return true;
		}
		
		
	}
	
	//entry for a cached node
	private static class CacheKey{
		
		//uses flyweight pattern to bound the explosiveness of memory
		private static Map<String, Map<Integer, CacheKey>> flyweight = new TreeMap<>();
		
		public static CacheKey get(String head, int startPosition) {
			Integer pos = Integer.valueOf(startPosition);
			Map<Integer, CacheKey> posMap = null;
			if(flyweight.containsKey(head)) {
				posMap = flyweight.get(head);
				if(posMap.containsKey(pos))
					return posMap.get(pos);
			}
			
			if(posMap == null) {
				//uses a HashMap for the second level
				posMap = new HashMap<>();
				flyweight.put(head, posMap);
			}
			
			CacheKey cacheEntry = new CacheKey(head, startPosition);
			posMap.put(pos, cacheEntry);
			
			return cacheEntry;
		}
		
		public final String product;
		public final int startPosition;
		
		private CacheKey(String product, int startPosition) {
			this.product = product;
			this.startPosition = startPosition;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((product == null) ? 0 : product.hashCode());
			result = prime * result + startPosition;
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (product == null) {
				if (other.product != null)
					return false;
			} else if (!product.equals(other.product))
				return false;
			if (startPosition != other.startPosition)
				return false;
			return true;
		}
		
	}
	
	//cache init - LRU policy (Least Recently Used) 
	private Map<CacheKey, SyntaxNode> cache = new HashMap<>();
	private LinkedHashSet<CacheKey> lruBuffer = new LinkedHashSet<>();
	
	/**
	 * The max cache size
	 */
	private int cacheSize;
	
	/**
	 * The lexer used by the parser
	 */
	private Lexer lexer;
	
	/**
	 * The axiom (i.e. the rule from which the parsing will start)
	 */
	public String axiom;
	
	/**
	 * Heads of the added rules
	 */
	private Map<String, ArrayList<Rule>> rules = new LinkedHashMap<>();
	
	/**
	 * When the parsing is unsuccessful, this set contains all the expected terminal tokens
	 */
	private Set<ExpectedTerminal> expected;
	
	private String unexpectedToken;
	
	private int unexpectedPosition;
	
	/**
	 * Store the detected direct recursions (without forwarding the lexer).
	 * Used to detect infinite recursions.
	 */
	private Map<String, Set<String>> leftRec = new HashMap<>();
	
	/**
	 * Construct a StandardParser with a cache size of 100 nodes
	 */
	public StandardParser() {
		this.cacheSize = 100;
	}
	
	/**
	 * Construct a StandardParser with the specified cache size
	 * @param cacheSize the cache size
	 */
	public StandardParser(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public void setLexer(Lexer lexer){
		this.lexer = lexer;
	}
	
	@Override
	public Lexer getLexer() {
		return lexer;
	}

	public void addRule(String head, String... production) throws InfiniteRecursionException, InvalidRuleNameException{
		addRule(head, Arrays.asList(production));
	}
	
	public void addRule(String head, List<String> production) throws InfiniteRecursionException, InvalidRuleNameException{
		if(!head.matches(EditableParser.RULE_PATTERN))
			throw new InvalidRuleNameException(head, EditableParser.RULE_PATTERN);
		
		if(production.size() > 0){
			//searches for infinite left recursions
			String first = production.get(0);
			if(!first.startsWith(EditableParser.TERMINAL_PREFIX)){
				if(!leftRec.containsKey(head))
					leftRec.put(head, new TreeSet<>());
				leftRec.get(head).add(first);
				
				if(pathExists(first, head))
					throw new InfiniteRecursionException(head, first);
				
			}
		}
		
		ArrayList<Rule> current = null;
				
		if(rules.containsKey(head))
			current = rules.get(head);
		else{
			current = new ArrayList<>();
			rules.put(head, current);
		}
		
		Rule rule = new Rule(head, production);
		
		if(!current.contains(rule))
			current.add(rule);

		if(axiom == null) axiom = head;
	}
	
	/**
	 * Recursively searches for a path from a rule to another.
	 * Helper method used to detect infinite recursions.
	 * @param r1 source rule
	 * @param r2 destination rule
	 * @return true if a path exists, false otherwise
	 */
	private boolean pathExists(String r1, String r2){
		if(r1.equals(r2))
			return true;
		
		if(leftRec.containsKey(r1)){
			for(String next : leftRec.get(r1)){
				if(pathExists(next, r2))
					return true;
			}
		}
		
		return false;
	}

	public void setAxiom(String axiom){
		this.axiom = axiom;
	}
	
	public SyntaxTree parse() throws UnexpectedSymbolException {
		expected = new HashSet<>();
		lexer.setStart(0);
		
		SyntaxTree root = parse(axiom);
		
		cache.clear();
		lruBuffer.clear();
		
		if(!lexer.next()) {
			return root;
		}
		
		throw new UnexpectedSymbolException(expected, unexpectedToken, unexpectedPosition, lexer.input());
	}
	
	private SyntaxNode parse(String ruleHead) {
		CacheKey cacheKey = CacheKey.get(ruleHead, lexer.start());
		if(cache.containsKey(cacheKey)) {
			if(DEBUG_PRINT) System.out.println(" << cache hit! >>");
			
			lruBuffer.remove(cacheKey);
			lruBuffer.add(cacheKey);
			return cache.get(cacheKey);
		}
		
		ArrayList<Rule> rules = this.rules.get(ruleHead);
		if(rules == null)
			return null;
		int lexerStart = lexer.start(), lexerPos = lexerStart;
		
		SyntaxNode node = new SyntaxNode(lexer);
		
		boolean accept = false;
		
		for(Rule rule : rules) {
			accept = true;
			
			for(String product : rule) {
				lexer.setStart(lexerPos);
				lexer.next();
				
				if(product.startsWith(TERMINAL_PREFIX)) { //terminal
					String type = product.substring(1);
					if(lexer.next()) {
						String token = lexer.token(type);
						if(token != null) {
							if(unexpectedPosition < lexer.end())
								expected.clear();
							
							SyntaxNode son = new SyntaxNode(lexer);
							
							son.terminal = true;
							son.type = type;
							son.start = lexer.start();
							son.end = lexer.end();
							son.similarTypes = lexer.similarTypes();
							
							node.nexts.addLast(son);
							
							lexerPos = son.end()+1;
							if(DEBUG_PRINT) System.out.println("accept terminal: " + type + " (\"" + token + "\")");
							
							continue;
						}
					}
					
					if(unexpectedPosition < lexer.start())
						expected.clear();
					
					if(expected.isEmpty() || unexpectedPosition == lexer.start()){
						if(expected.isEmpty()) {
							unexpectedPosition = lexer.start();
							unexpectedToken = lexer.token();
						}
						expected.add(new ExpectedTerminal(unexpectedPosition, type, lexer.description(type)));
					}
					
					if(DEBUG_PRINT) System.out.println("error terminal: " + type + ", read=" + lexer.token());
					accept = false;
				}
				else {	//non-terminal

					if(DEBUG_PRINT) System.out.println("entering non-terminal: " + product);
					SyntaxNode son = parse(product);
					if(son != null) {
						if(DEBUG_PRINT) System.out.println("accept non-terminal: " + product);
						lexerPos = son.end+1;
						node.nexts.addLast(son);
					}
					else {
						if(DEBUG_PRINT) System.out.println("error non-terminal: " + product);
						accept = false;
					}
					
				}
				
				if(!accept)
					break;
				
			}
			
			if(accept)
				break;
			else {
				node.nexts.clear();
				lexerPos = lexerStart;
				if(DEBUG_PRINT) System.out.println("pos=" + lexerPos);
			}
		}
		
		if(!accept) {
			return null;
		}
		
		lexer.setStart(lexerPos);
		node.type = ruleHead;
		node.start = lexerStart;
		node.end = lexerPos-1;
		
		if(lexerPos >= lexerStart) {
			//LRU removing
			if(lruBuffer.size() >= cacheSize) {
				cacheKey = lruBuffer.iterator().next();
				lruBuffer.remove(cacheKey);
				cache.remove(cacheKey);
			}
			
			//caching
			cacheKey = CacheKey.get(ruleHead, lexerStart);
			cache.put(cacheKey, node);
			
			//LRU buffering
			lruBuffer.add(cacheKey);
		}
		
		return node;
	}
	
	/*
	public static void main(String[] args) throws IOException {
		String expr = "b c a c";
		StandardLexer lexer = new StandardLexer();
		StandardParser parser = new StandardParser();
		lexer.setInput(expr);
		parser.setLexer(lexer);

		lexer.addType("a", "a", "a");
		lexer.addType("b", "b", "b");
		lexer.addType("c", "c", "c");
		lexer.addType("nonSkippable", "[^\\s]", "non skippable");

		try {
			parser.addRule("A", "B", "$b" , "C");
			parser.addRule("A", "B", "$a" , "C");
			parser.addRule("A", "B", "C");
			parser.addRule("B", "$b", "$b");
			parser.addRule("B", "$b", "$c");
			
			parser.addRule("C", "$c");
			parser.addRule("C");

		} catch (InfiniteRecursionException e) {
			e.printStackTrace();
			return;
		}
		catch (InvalidRuleNameException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("expression: " + expr);
		SyntaxTree tree;
		try {
			tree = parser.parse();
		} catch (UnexpectedSymbolException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("parsing: " + (tree != null));
		System.out.println(tree);
		
	}*/
}
