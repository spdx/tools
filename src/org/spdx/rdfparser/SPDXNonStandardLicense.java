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
 * A non-standard license which is valid only within an SPDXAnalysis
 * @author Gary O'Neall
 *
 */
public class SPDXNonStandardLicense extends SPDXLicense {

	/**
	 * @param model
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXNonStandardLicense(Model model, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(model, licenseInfoNode);
	}
	
	public SPDXNonStandardLicense(String id, String text) {
		super(id, text);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO);
		return super._createResource(model, type);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		// must be only the ID if we are to use this to create 
		// parseable license strings
		return this.id;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SPDXNonStandardLicense)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		SPDXNonStandardLicense comp = (SPDXNonStandardLicense)o;
		if (this.id == null) {
			return (comp.getId() == null);
		} else {
			return (this.id.equals(comp.getId()));			
		}
	}

	/**
	 * @return
	 */
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		String id = this.getId();
		if (id == null || id.isEmpty()) {
			retval.add("Missing required license ID");
		}
		String licenseText = this.getText();
		if (licenseText == null || licenseText.isEmpty()) {
			retval.add("Missing required license text");
		}
		return retval;
	}
}
