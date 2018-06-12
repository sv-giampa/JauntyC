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

package jointyc.analysis.semantic;

import java.util.List;

import jointyc.analysis.parser.SyntaxTree;
import jointyc.analysis.semantic.annotation.TerminalToken;
import jointyc.analysis.semantic.exception.SemanticException;

/**
 * Basing on the Template Method pattern, the implementors of this interface
 * must provide the semantic actions for the terminal and non-terminal symbols
 * in the syntax tree. Applying a semantic action could lead to the production
 * of some semantic result.The semantic actions are provided as custom methods
 * of the implementor without needing to override any method of this interface.
 * Altought the methods that provide semantic actions must be annotated. Each
 * annotated method can be exposed with any access modifier.<br/>
 * <br/>
 * Terminal tokens' semantic interpreter methods must be annotated with the
 * {@link TerminalToken} annotation and must return the object biult by semantic
 * operations. A terminal interpreter method, can take no arguments or at most
 * one argument of type {@link SyntaxTree}. Through this single argument, the
 * method can access to the token string, and others token properties(see
 * {@link SyntaxTree} t get more information about). The method can return
 * anything that represents the token semantic result, or void.<br/>
 * <br/>
 * A non-terminal token semantic interpreter method must be annotated with the
 * {@link NonTerminalToken} annotation. The method can take many arguments as it
 * needs and corresponding to the types of the tokens in the production of the
 * rule which is annotated with. The method can return anything that represents
 * the token semantic result, or void.
 * 
 * 
 * @author Salvatore Giampà
 *
 */
public interface Interpreter {
}
