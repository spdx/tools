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

import java.util.HashSet;
import java.util.Iterator;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A set of licenses where all of the licenses apply
 * @author Gary O'Neall
 *
 */
public class ConjunctiveLicenseSet extends LicenseSet {

	/**
	 * @param modelContainer container which includes the license
	 * @param node Node that defines the conjunctive license set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ConjunctiveLicenseSet(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
	}

	/**
	 * @param conjunctiveLicenses
	 */
	public ConjunctiveLicenseSet(AnyLicenseInfo[] conjunctiveLicenses) {
		super(conjunctiveLicenses);
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
				sb.append(" AND ");
			}
			moreThanOne = true;
			sb.append(iter.next().toString());
		}
		sb.append(')');
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		// Calculate a hashcode by XOR'ing all of the hashcodes of the license set
		int retval = 41;	// Prime number
		AnyLicenseInfo[] allMembers = this.getFlattenedMembers();
		for (int i = 0; i < allMembers.length; i++) {
			retval = retval ^ allMembers[i].hashCode();
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
		if (!(o instanceof ConjunctiveLicenseSet)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		ConjunctiveLicenseSet comp = (ConjunctiveLicenseSet)o;
		AnyLicenseInfo[] compInfos = comp.getFlattenedMembers();
		AnyLicenseInfo[] myInfos = this.getFlattenedMembers();
		if (compInfos.length != myInfos.length) {
			return false;
		}
		for (int j = 0; j < myInfos.length; j++) {
			AnyLicenseInfo li = myInfos[j];
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


	/**
	 * Conjunctive license sets can contain other conjunctive license sets as members.  Logically,
	 * the members of these "sub-conjunctive license sets" could be direct members and have the same
	 * meaning.
	 * @return all members "flattening out" conjunctive license sets which are members of this set
	 */
	private AnyLicenseInfo[] getFlattenedMembers() {
		HashSet<AnyLicenseInfo> retval = new HashSet<AnyLicenseInfo>();	// Use a set since any duplicated elements would be still considered equal
		Iterator<AnyLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			AnyLicenseInfo li = iter.next();
			if (li instanceof ConjunctiveLicenseSet) {
				// we need to flatten this out
				AnyLicenseInfo[] members = ((ConjunctiveLicenseSet)li).getFlattenedMembers();
				for (int i = 0; i < members.length; i++) {
					retval.add(members[i]);
				}
			} else {
				retval.add(li);
			}
		}
		return retval.toArray(new AnyLicenseInfo[retval.size()]);
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
		return new ConjunctiveLicenseSet(clonedSet);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (compare == this) {
			return true;
		}
		if (!(compare instanceof ConjunctiveLicenseSet)) {
			return false;
		}
		return super.equivalent(compare);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		// Use anonomous nodes
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_CONJUNCTIVE_LICENSE_SET);
	}
}
