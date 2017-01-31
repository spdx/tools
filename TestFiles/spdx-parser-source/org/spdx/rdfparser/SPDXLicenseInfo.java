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

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * This abstract class represents several ways of describing licensing information.
 * License info can be described as a set of conjunctive licenses (where all licenses
 * terms must apply), a set of disjunctive licenses (where there is a choice of one
 * license among the set described) or a specific license.  The specific license can
 * be an SPDX standard license or a non-standard license.
 * @author Gary O'Neall
 *
 */
public abstract class SPDXLicenseInfo {

	//TODO: Consider adding a comment text string

	Model model = null;
	Node licenseInfoNode = null;
	Resource resource = null;
	/**
	 * Create a new LicenseInfo object where the information is copied from
	 * the model at the LicenseInfo node
	 * @param model
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException
	 */
	SPDXLicenseInfo(Model model, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		this.model = model;
		this.licenseInfoNode = licenseInfoNode;
		resource = convertToResource(model, licenseInfoNode);
	}

	/**
	 * Convert a node to a resource
	 * @param cmodel
	 * @param cnode
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	private Resource convertToResource(Model cmodel, Node cnode) throws InvalidSPDXAnalysisException {
		if (cnode.isBlank()) {
			return cmodel.createResource(cnode.getBlankNodeId());
		} else if (cnode.isURI()) {
			return cmodel.createResource(cnode.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Can not create a license from a literal"));
		}
	}


	SPDXLicenseInfo() {
		this.model = null;
		this.licenseInfoNode = null;
		this.resource = null;
	}

	/**
	 * If a resource does not already exist in this model for this object,
	 * create a new resource and populate it.  If the resource does exist,
	 * return the existing resource.
	 * @param model
	 * @return resource created from the model
	 */
	public Resource createResource(Model model) {
		if (this.model != null &&
				this.model.equals(model) &&
				this.licenseInfoNode != null &&
				this.resource != null) {
			return resource;
		} else {
			this.model = model;
			Resource retval = _createResource(model);
			this.licenseInfoNode = retval.asNode();
			this.resource = retval;
			return retval;
		}
	}

	/**
	 * Internal implementation of create resource which is subclass specific
	 * @param model
	 * @return
	 */
	protected abstract Resource _createResource(Model model);

	// force subclasses to implement toString
	public abstract String toString();

	// force subclasses to implement equals
	public abstract boolean equals(Object o);

	/**
	 * @return
	 */
	public abstract ArrayList<String> verify();
}
