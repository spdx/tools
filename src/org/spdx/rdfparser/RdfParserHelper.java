/**
 * Copyright (c) 2015 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.rdfparser;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Helper class with common functions used by the RDF Parser
 * @author Gary O'Neall
 *
 */
public class RdfParserHelper {
	
	/**
	 * Convert a node to a resource
	 * @param cmodel
	 * @param cnode
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static Resource convertToResource(Model cmodel, Node cnode) throws InvalidSPDXAnalysisException {
		if (cnode.isBlank()) {
			return cmodel.createResource(cnode.getBlankNodeId());
		} else if (cnode.isURI()) {
			return cmodel.createResource(cnode.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Can not create a resource from a literal"));
		}
	}

}
