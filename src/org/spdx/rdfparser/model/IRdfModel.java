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
package org.spdx.rdfparser.model;

import java.util.List;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

import org.apache.jena.rdf.model.Resource;

/**
 * Interface to translate from a Java model to a Jena RDF model
 * @author Gary O'Neall
 *
 */
public interface IRdfModel {

	/**
	 * Create a resource from the Java model object
	 * @param modelContainer Contains the Jena model where to create the resource
	 * @param parentProperty
	 * @return The created resource
	 * @throws InvalidSPDXAnalysisException
	 */
	public Resource createResource(IModelContainer modelContainer) throws InvalidSPDXAnalysisException;

	/**
	 * @return List of validation errors for any non SPDX compliant properties.
	 */
	public List<String> verify();

	/**
	 * Returns true if the compare object contains properties which would be equal if they were contained in the same RDF Model
	 * @param compare
	 * @return
	 */
	public boolean equivalent(IRdfModel compare);

	/**
	 * Called to signal that there are multiple objects representing the same node
	 */
	public void setMultipleObjectsForSameNode();

	/**
	 * Called to signal that a newly created resource is only used for a single node
	 */
	public void setSingleObjectForSameNode();
}
