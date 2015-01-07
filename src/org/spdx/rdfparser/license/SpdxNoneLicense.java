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

import java.util.ArrayList;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A special license meaning that no license was found
 * @author Gary O'Neall
 *
 */
public class SpdxNoneLicense extends AnyLicenseInfo {
	
	static final int NONE_LICENSE_HASHCODE = 147; // prime number - all none licenses should have the same hashcde

	/**
	 * @param model
	 * @param node
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxNoneLicense(Model model, Node node) throws InvalidSPDXAnalysisException {
		super(model, node);
	}
	
	public SpdxNoneLicense() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_NONE);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		return LicenseInfoFactory.NONE_LICENSE_NAME;
	}
	
	@Override
	public int hashCode() {
		return NONE_LICENSE_HASHCODE;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		return (o instanceof SpdxNoneLicense);		// All Instances of this type are considered equal
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#clone()
	 */
	@Override
	public AnyLicenseInfo clone() {
		return new SpdxNoneLicense();
	}

}
