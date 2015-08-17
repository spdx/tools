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

import org.spdx.rdfparser.model.IRdfModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Interface for a a class that contains an RDF model
 * 
 * @author Gary O'Neall
 *
 */
public interface IModelContainer {

	/**
	 * @return the RDF model
	 */
	Model getModel();

	/**
	 * @return Namespace for document
	 */
	String getDocumentNamespace();

	/**
	 * @return The next available SPDX element reference ID.  The ID
	 * is unique within a given model.
	 */
	String getNextSpdxElementRef();

	/**
	 * Returns true if the element reference already exists in the model
	 * @param id
	 * @return
	 */
	boolean spdxElementRefExists(String elementRef);

	/**
	 * Notifies the model container that a new element ref is in use.
	 * This must be called for all new element references to prevent
	 * duplication of elements.
	 * @param elementRef
	 * @throws InvalidSPDXAnalysisException 
	 */
	void addSpdxElementRef(String elementRef) throws InvalidSPDXAnalysisException;

	/**
	 * Translate an external document namespace URI to an external document ID 
	 * @param externalNamespace
	 * @return
	 */
	String documentNamespaceToId(String externalNamespace);

	/**
	 * Translate an external document ID to the external document's namespace
	 * @param docId
	 * @return
	 */
	String externalDocumentIdToNamespace(String docId);

	/**
	 * Create a resource in the model
	 * @param duplicate A duplicate resource who's node and resource should be returned
	 * @param uri If not null, the URI for the resource - if null, an anonomous resource will be used
	 * @param type Type of the resource
	 * @param modelObject the object representing this node
	 * @return
	 */
	Resource createResource(Resource duplicate, String uri, Resource type, IRdfModel modelObject);

	/**
	 * Add a node object to the container.  This is for containers which keep track
	 * of which object are related to which nodes
	 * @return true if there are multiple objects for this node
	 * @param node
	 * @param rdfModelObject
	 */
	boolean addCheckNodeObject(Node node, IRdfModel rdfModelObject);
}
