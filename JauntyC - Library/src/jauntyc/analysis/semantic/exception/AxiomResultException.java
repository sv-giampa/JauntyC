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

package jauntyc.analysis.semantic.exception;

/**
 * Usually thrown when the last semantic action of an interpreter left more than one object in the result buffer.
 * @author Salvatore Giampà
 *
 */
public class AxiomResultException extends RuntimeException{

	private static final long serialVersionUID = -9131756183397577894L;

	private static final String MESSAGE = "Assiome's semantic action must return at most one result";
	
	public AxiomResultException() {
		super(MESSAGE);
	}
}
