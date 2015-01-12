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

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Interface to translate from a Java model to a Jena RDF model
 * @author Gary O'Neall
 *
 */
public interface IRdfModel {
	
	/**
	 * Create a resource from the Java model object
	 * @param model Jena model to create the resource
	 * @param uri Unique resource identifier for the resource.  If null, an anonymous node is created.
	 * @param parentProperty
	 * @return The created resource
	 */
	public Resource createResource(Model model, String uri);
	
	/**
	 * @return List of validation errors for any non SPDX compliant properties.
	 */
	public ArrayList<String> verify();

}
