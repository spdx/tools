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

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * A set of licenses where there is a choice of one of the licenses in the set
 * @author Gary O'Neall
 *
 */
public class SPDXDisjunctiveLicenseSet extends SPDXLicenseSet {

	/**
	 * @param model
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException
	 */
	public SPDXDisjunctiveLicenseSet(Model model, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(model, licenseInfoNode);
	}

	/**
	 * @param disjunctiveLicenses
	 */
	public SPDXDisjunctiveLicenseSet(SPDXLicenseInfo[] disjunctiveLicenses) {
		super(disjunctiveLicenses);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#_createResource(org.apache.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET);
		return super._createResource(model, type);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		boolean moreThanOne = false;
		Iterator<SPDXLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			if (moreThanOne) {
				sb.append(" OR ");
			}
			moreThanOne = true;
			sb.append(iter.next().toString());
		}
		sb.append(")");
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SPDXDisjunctiveLicenseSet)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		SPDXDisjunctiveLicenseSet comp = (SPDXDisjunctiveLicenseSet)o;
		SPDXLicenseInfo[] compInfos = comp.getSPDXLicenseInfos();
		if (compInfos.length != this.licenseInfos.size()) {
			return false;
		}
		Iterator<SPDXLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			SPDXLicenseInfo li = iter.next();
			boolean found = false;
			for (int i = 0; i < compInfos.length; i++) {
				if (li.equals(compInfos[i])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
