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
import java.util.LinkedList;
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
 * The parsing procedure is iterative.
 * This parser uses a cache to avoid revisiting of syntax structures.
 * The cache uses the LRU policy (Least Recently Used) for node replacing.
 * The default dimension of the cache is set to 500 nodes. 
 * 
 * @author Salvatore Giampà
 *
 */
public class StandardParser implements EditableParser {

	private static final boolean DEBUG_PRINT = true;
	
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
		
		public String getProduct(int index) {
			return production.get(index);
		}
		
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
	private static class CacheEntry{
		
		//uses flyweight pattern to bound the explosiveness of memory
		private static Map<String, Map<Integer, CacheEntry>> flyweight = new TreeMap<>();;
		
		public static CacheEntry get(String head, int startPosition) {
			Integer pos = Integer.valueOf(startPosition);
			Map<Integer, CacheEntry> posMap = null;
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
			
			CacheEntry cacheEntry = new CacheEntry(head, startPosition);
			posMap.put(pos, cacheEntry);
			
			return cacheEntry;
		}
		
		public final String product;
		public final int startPosition;
		
		private CacheEntry(String product, int startPosition) {
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
			CacheEntry other = (CacheEntry) obj;
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
	 * Construct a StandardParser with a cache size of 500 nodes
	 */
	public StandardParser() {
		this.cacheSize = 500;
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
	
	//A data area for a recursive rule production validation
	private class StackData{
		String head;
		List<Rule> rules;
		int currentRule = 0;
		int currentProduct = 0;
		int lexerStart = 0;
		int lexerEnd = 0;
		SyntaxNode ret;
		SyntaxNode node;
		boolean call = false;
		List<SyntaxNode> nexts = new LinkedList<>();
		
		public StackData(String rule, int lexerPos) {
			this.rules = StandardParser.this.rules.get(rule);
			this.lexerEnd = this.lexerStart = lexerPos;
			this.head = rule;
		}
		
		public Rule getCurrentRule() {
			return rules.get(currentRule);
		}
	}
	
	public SyntaxTree parse() throws UnexpectedSymbolException{
		
		expected = new HashSet<>();
		LinkedList<StackData> stack = new LinkedList<>();
		
		//cache init - LRU policy (Least Recently Used) 
		Map<CacheEntry, SyntaxNode> cache = new HashMap<>();
		LinkedList<CacheEntry> lruBuffer = new LinkedList<>();
		
		//put root node
		lexer.setStart(0);
		StackData data = new StackData(axiom, lexer.start());
		data.node = new SyntaxNode(lexer);
		data.node.start = lexer.start();
		data.node.type = axiom;
		
		stack.push(data);
		
		while(stack.size() > 0) {
			data = stack.peek();
			
			//if the current node was computed in another try -> cache hit
			CacheEntry cacheKey = CacheEntry.get(data.head, data.lexerStart);
			if(cache.containsKey(cacheKey)) {
				stack.pop();
				SyntaxNode node = cache.get(cacheKey);
				if(stack.size() > 0) 
					stack.peek().ret = node;
				lexer.setStart(node.end+1);
				
				if(DEBUG_PRINT) System.out.println("<< cached node! >>");
				continue;
			}
			
			//returnig from a recursive call
			if(data.call) {
				data.call = false;
				if(data.ret != null) {
					//non-null result: success. go to next product
					data.nexts.add(data.ret);
					data.lexerEnd = data.ret.end+1;
					data.ret = null;
				}
				else {
					//null-result: fail, go to next rule
					data.nexts.clear();
					data.currentRule++;
					data.currentProduct = 0;
					lexer.setStart(data.lexerStart);
				}
			}
			
			if(data.rules == null || data.currentRule >= data.rules.size()) {
				stack.pop();
				
				//return from this call, fail
				if(DEBUG_PRINT) for(int i=0; i<stack.size()+1; i++) System.out.print("  ");
				if(DEBUG_PRINT) System.out.println("return unsuccess: " + data.head + " \n");
				
				if(stack.size() > 0)
					stack.peek().ret = null;
				data = null;
				continue;
			}
			
			//get current rule
			Rule rule = data.getCurrentRule();
			
			if(data.currentProduct >= rule.getProductionLength()) {
				for(SyntaxNode next : data.nexts)
					data.node.nexts.add(next);
				data.node.end = lexer.end();
				
				//return from this call, success
				stack.pop();
				if(stack.size() > 0) 
					stack.peek().ret = data.node;

				if(DEBUG_PRINT) for(int i=0; i<stack.size()+1; i++) System.out.print("  ");
				if(DEBUG_PRINT) System.out.println("return success: "+data.head+" \n");
				
				//LRU removing
				if(lruBuffer.size() >= cacheSize)
					cache.remove(lruBuffer.removeLast());
				
				//caching
				CacheEntry cn = CacheEntry.get(data.head, data.lexerStart);
				cache.put(cn, data.node);
				
				//LRU buffering
				lruBuffer.addFirst(cn);
				
				continue;
			}
			
			//get current product
			String product = rule.getProduct(data.currentProduct);
			
			if(DEBUG_PRINT) for(int i=0; i<stack.size(); i++) System.out.print("  ");
			if(DEBUG_PRINT) System.out.print("rule: " + rule + " : currentProduct=" + product + "(" + data.currentProduct + ") : ");
			
			data.currentProduct++;

			if(product.startsWith(TERMINAL_PREFIX)) {
				if(DEBUG_PRINT) System.out.println("terminal");
				
				//terminal
				product = product.substring(1);
				if(lexer.next()) {
					String token = lexer.token(product);
					if(token != null) {
						if(unexpectedPosition < data.lexerEnd)
							expected.clear();
						
						SyntaxNode node = new SyntaxNode(lexer);
						
						node.terminal = true;
						node.type = product;
						node.start = lexer.start();
						node.end = lexer.end();
						node.similarTypes = lexer.similarTypes();
						
						data.lexerEnd = lexer.end()+1;
						data.nexts.add(node);
						
						continue;
					}
					else {
						if(DEBUG_PRINT) for(int i=0; i<stack.size(); i++) System.out.print("  ");
						if(DEBUG_PRINT) System.out.println("-- terminal fail: matching (found \""+lexer.token()+"\") --\n");
					}
				}
				else {
					if(DEBUG_PRINT) for(int i=0; i<stack.size(); i++) System.out.print("  ");
					if(DEBUG_PRINT) System.out.println("-- terminal fail: no input --\n");
				}
				
				if(expected.size() == 0 || unexpectedPosition == data.lexerEnd){
					expected.add(new ExpectedTerminal(data.lexerEnd, product, lexer.description(product)));
					unexpectedPosition = data.lexerEnd;
					unexpectedToken = lexer.token();
					if(DEBUG_PRINT) for(int i=0; i<stack.size(); i++) System.out.print("  ");
					if(DEBUG_PRINT) System.out.println(String.format("-- expected='%s', data.lexerEnd=%s, unexpectedPosition=%s\n", product, data.lexerEnd, unexpectedPosition));
					
				}
				
				data.currentRule++;
				data.currentProduct=0;
				data.nexts.clear();
				data.lexerEnd = data.lexerStart;
				lexer.setStart(data.lexerStart);
			}
			else {
				if(DEBUG_PRINT) System.out.println("non-terminal");
				if(DEBUG_PRINT) System.out.println();
				
				//non-terminal
				data.call = true;
				
				data = new StackData(product, data.lexerEnd);
				data.node = new SyntaxNode(lexer);
				data.node.start = data.lexerEnd;
				data.node.type = product;

				stack.push(data);
			}
			
		}
		
		lexer.next();
		if(data != null && lexer.end() == lexer.input().length()-1) {
			//parsing successful
			SyntaxTree root = data.node;
			return root;
		}
		else throw new UnexpectedSymbolException(expected, unexpectedToken, unexpectedPosition, lexer.input());
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
