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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Ddefines a simple, double direction, iterator over the sons of syntax node.</br></br>
 * It does not impement the remove() operation. Trying the use of this, will throw an IllegalStateException.
 * @author Salvatore Giampà
 *
 */
public class SyntaxIterator implements Iterator<SyntaxTree> {
	ListIterator<? extends SyntaxTree> it;
	
	/**
	 * Construct a new SyntaxIterator. This constructor should be used only by an implementation of {@link SyntaxTree}.
	 * @param list the list of sons which backs the node
	 * @param fromLast true to start from the last node, false to start from the first one
	 */
	public SyntaxIterator(List<? extends SyntaxTree> list, boolean fromLast){
		if(fromLast) 
			this.it = list.listIterator(list.size());
		else
			this.it = list.listIterator();
	}

	
	/**
	 * Returns true if the node has more sons. (In other words, returns true if next() would return a son rather than throwing an exception.)
	 * @return true if the node has more sons
	*/
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	/**
	 * Returns the next son in the iteration.
	 * @return the next son in the iteration
	 */
	@Override
	public SyntaxTree next() {
		return it.next();
	}

	/**
	 * Returns true if the node has more sons. (In other words, returns true if previous() would return a son rather than throwing an exception.)
	 * @return true if the node has more sons
	*/
	public boolean hasPrevious() {
		return it.hasPrevious();
	}

	/**
	 * Returns the previous son in the iteration.
	 * @return the previous son in the iteration
	 */
	public SyntaxTree previous() {
		return it.previous();
	}

}
