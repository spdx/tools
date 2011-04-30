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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Special class of license to represent an ambiguous license in the file or packages
 * @author Gary O'Neall
 *
 */
public class SPDXAmbiguousLicense extends SPDXLicenseInfo {

	/**
	 * @param model
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException
	 */
	public SPDXAmbiguousLicense(Model model, Node licenseInfoNode)
			throws InvalidSPDXAnalysisException {
		super(model, licenseInfoNode);
	}

	/**
	 * 
	 */
	public SPDXAmbiguousLicense() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_AMBIGUOUS);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		return SPDXLicenseInfoFactory.AMBIGUOUS_LICENSE_NAME;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof SPDXAmbiguousLicense) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		return new ArrayList<String>();
	}

}
