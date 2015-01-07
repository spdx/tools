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

import java.util.Iterator;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A set of licenses where there is a choice of one of the licenses in the set
 * @author Gary O'Neall
 *
 */
public class DisjunctiveLicenseSet extends LicenseSet {

	/**
	 * @param model
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException 
	 */
	public DisjunctiveLicenseSet(Model model, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(model, licenseInfoNode);
	}

	/**
	 * @param disjunctiveLicenses
	 */
	public DisjunctiveLicenseSet(AnyLicenseInfo[] disjunctiveLicenses) {
		super(disjunctiveLicenses);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) throws InvalidSPDXAnalysisException {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET);
		return super._createResource(model, type);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		boolean moreThanOne = false;
		Iterator<AnyLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			if (moreThanOne) {
				sb.append(" OR ");
			}
			moreThanOne = true;
			sb.append(iter.next().toString());
		}
		sb.append(')');
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		int retval = 41;	// Prime number
		Iterator<AnyLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			AnyLicenseInfo li = iter.next();
			retval = retval ^ li.hashCode();
		}
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof DisjunctiveLicenseSet)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		DisjunctiveLicenseSet comp = (DisjunctiveLicenseSet)o;
		AnyLicenseInfo[] compInfos = comp.getMembers();
		if (compInfos.length != this.licenseInfos.size()) {
			return false;
		}
		Iterator<AnyLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			AnyLicenseInfo li = iter.next();
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
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#clone()
	 */
	@Override
	public AnyLicenseInfo clone() {
		AnyLicenseInfo[] clonedSet = new AnyLicenseInfo[this.licenseInfos.size()];
		Iterator<AnyLicenseInfo> iter = this.licenseInfos.iterator();
		int i = 0;
		while (iter.hasNext()) {
			clonedSet[i++] = iter.next().clone();
		}
		return new DisjunctiveLicenseSet(clonedSet);
	}
}
