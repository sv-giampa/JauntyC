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

/**
 * Defines an editable lexer, that allows to edit its token types and properties.
 * @author Salvatore Giampà
 *
 */
public interface EditableLexer extends Lexer {

	/**
	 * Add a new token type.
	 * @param type the type name
	 * @param regex the regular expression that match the new type
	 * @param description a printable description for this type
	 * @param skip true if this token type must be skipped by the lexer
	 */
	void addType(String type, String regex, String description, boolean skip);
	
	/**
	 * Add a new non-skippable token type.
	 * @param type the type name
	 * @param regex the regular expression that match the new type
	 * @param description a printable description for this type
	 */
	default void addType(String type, String regex, String description) {
		addType(type, regex, description, false);
	}
	
	/**
	 * 
	 * Add a new non-skippable token type.
	 * Description is supposed to be null
	 * @param type the type name
	 * @param regex the regular expression that match the new type
	 */
	default void addType(String type, String regex){
		addType(type,regex, null, false);
	}
	
	/**
	 * 
	 * Add a new token type.
	 * Description is supposed to be null
	 * @param type the type name
	 * @param regex the regular expression that match the new type
	 * @param skip tells the lexer if the type is skippable (the tokens that match it are ignored)
	 */
	default void addType(String type, String regex, boolean skip){
		addType(type, regex, null, skip);
	}
	
	/**
	 * Reset the lexer editing, clear the type list.
	 */
	void resetTypes();
	
}
