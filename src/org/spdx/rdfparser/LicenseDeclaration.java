/**
 * Copyright (c) 2011 Source Auditor Inc.
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

import java.util.ArrayList;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Model for a License Declaration
 * @author Gary O'Neall
 *
 */
public class LicenseDeclaration {

	private String name;	// this is the license ID
	private String[] disjunctiveLicenses;
	
	/**
	 * Construct a new license declaration and populate the properties based on the licenseNode
	 * @param licenseNode Node in the RDF graph representing the license declaration
	 */
	public LicenseDeclaration(Node licenseNode, Model model) {
		// name
		Node p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_LICENSE_ID).asNode();
		Triple m = Triple.createMatch(licenseNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// disjunctiveLicenses
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_DISJUNCTIVE_LICENSE).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);
		ArrayList<String> als = new ArrayList<String>();
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			als.add(t.getObject().toString(false));
		}
		this.disjunctiveLicenses = als.toArray(new String[als.size()]);
	}
	
	public LicenseDeclaration (String name, String[] disjunctiveLicenses) {
		this.name = name;
		this.disjunctiveLicenses = disjunctiveLicenses;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the disjunctiveLicenses
	 */
	public String[] getDisjunctiveLicenses() {
		return this.disjunctiveLicenses;
	}

	/**
	 * @param disjunctiveLicenses the disjunctiveLicenses to set
	 */
	public void setDisjunctiveLicenses(String[] disjunctiveLicenses) {
		this.disjunctiveLicenses = disjunctiveLicenses;
	}

	/**
	 * Populates a Jena RDF model with the information from this license declaration
	 * @param licenseResource
	 * @param model
	 */
	public void populateModel(Resource licenseResource, Model model) {
		// name
		Property nameProperty = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE,
				SPDXAnalysis.PROP_LICENSE_ID);
		licenseResource.addProperty(nameProperty, this.name);
		// disjunctive licenses
		if (this.disjunctiveLicenses != null) {
			for (int i = 0; i < this.disjunctiveLicenses.length; i++) {
				Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, 
						SPDXAnalysis.PROP_DISJUNCTIVE_LICENSE);
				licenseResource.addProperty(p, this.disjunctiveLicenses[i]);
			}
		}
	}
}
