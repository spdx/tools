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
package org.spdx.rdfparser.license;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.RdfModelObject;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * This abstract class represents several ways of describing licensing information.
 * License info can be described as a set of conjunctive licenses (where all licenses
 * terms must apply), a set of disjunctive licenses (where there is a choice of one
 * license among the set described) or a specific license.  The specific licenses
 * are of a SimpleLicensingInfoType
 * @author Gary O'Neall
 *
 */
public abstract class AnyLicenseInfo extends RdfModelObject {
	
	static final Logger logger = Logger.getLogger(AnyLicenseInfo.class.getName());

	/**
	 * Create a new LicenseInfo object where the information is copied from
	 * the model at the LicenseInfo node
	 * @param modelContainer container which includes the license
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException 
	 */
	AnyLicenseInfo(IModelContainer modelContainer, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseInfoNode);
	}


	AnyLicenseInfo() {
		super();
	}
	
	@Override
	public abstract void populateModel() throws InvalidSPDXAnalysisException;
	
	// force subclasses to implement toString
	@Override
    public abstract String toString();
	
	// force subclasses to implement equals
	@Override
    public abstract boolean equals(Object o);
	
	// force subclasses to implement hashcode
	@Override
    public abstract int hashCode();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
    public abstract AnyLicenseInfo clone();


	/**
	 * @return Resource for the license, or null if no resource has been created
	 */
	public Resource getResource() {
		return this.resource;
	}
}
