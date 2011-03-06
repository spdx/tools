/**
 * Copyright (c) 2011 Source Auditor Inc.
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
		Node p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_LICENSE_ID).asNode();
		Triple m = Triple.createMatch(licenseNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// disjunctiveLicenses
		p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_DISJUNCTIVE_LICENSE).asNode();
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
		Property nameProperty = model.createProperty(SPDXDocument.SPDX_NAMESPACE,
				SPDXDocument.PROP_LICENSE_ID);
		licenseResource.addProperty(nameProperty, this.name);
		// disjunctive licenses
		if (this.disjunctiveLicenses != null) {
			for (int i = 0; i < this.disjunctiveLicenses.length; i++) {
				Property p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, 
						SPDXDocument.PROP_DISJUNCTIVE_LICENSE);
				licenseResource.addProperty(p, this.disjunctiveLicenses[i]);
			}
		}
	}
}
