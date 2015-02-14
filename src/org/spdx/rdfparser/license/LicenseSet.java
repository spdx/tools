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
import java.util.HashSet;
import java.util.Iterator;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A specific form of license information where there is a set of licenses
 * represented
 * @author Gary O'Neall
 *
 */
public abstract class LicenseSet extends AnyLicenseInfo {
	
	protected HashSet<AnyLicenseInfo> licenseInfos = new HashSet<AnyLicenseInfo>();

	/**
	 * @param modelContainer container which includes the license
	 * @param licenseInfoNode Node in the RDF model which defines the licenseSet
	 * @throws InvalidSPDXAnalysisException 
	 */
	public LicenseSet(IModelContainer modelContainer, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseInfoNode);
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER).asNode();
		Triple m = Triple.createMatch(licenseInfoNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseInfos.add(LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, t.getObject()));
		}
	}


	/**
	 * @param licenseInfos Set of licenses
	 */
	public LicenseSet(AnyLicenseInfo[] licenseInfos) {
		super();
		if (licenseInfos != null) {
			for (int i = 0; i < licenseInfos.length; i++) {
				this.licenseInfos.add(licenseInfos[i]);
			}
		}
	}


	/**
	 * Create a resource for the license set
	 * @param type type of license set
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	protected Resource _createResource(Resource type) throws InvalidSPDXAnalysisException {
		Resource r = model.createResource(type);
		Property licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
		Iterator<AnyLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			Resource licResource = iter.next().createResource(modelContainer);
			r.addProperty(licProperty, licResource);
		}
		return r;
	}
	
	/**
	 * Sets the members of the license set.  Clears any previous members
	 * @param licenseInfos New members for the set
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setMembers(AnyLicenseInfo[] licenseInfos) throws InvalidSPDXAnalysisException {
		this.licenseInfos.clear();
		if (licenseInfos != null) {
			for (int i = 0; i < licenseInfos.length; i++) {
				this.licenseInfos.add(licenseInfos[i]);
			}
		}
		if (model != null && licenseInfoNode != null) {
			// delete any previous created
			Property licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
			model.removeAll(resource, licProperty, null);

			licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
			for (int i = 0; i < licenseInfos.length; i++) {
				Resource licResource = licenseInfos[i].createResource(this.modelContainer);
				resource.addProperty(licProperty, licResource);
			}
		}
	}
	
	/**
	 * @return Members of the license set
	 */
	public AnyLicenseInfo[] getMembers() {
		AnyLicenseInfo[] retval = new AnyLicenseInfo[this.licenseInfos.size()];
		retval = this.licenseInfos.toArray(retval);
		return retval;
	}	
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		Iterator<AnyLicenseInfo> iter = licenseInfos.iterator();
		while (iter.hasNext()) {
			retval.addAll(iter.next().verify());
		}
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof LicenseSet)) {
			return false;
		}
		return RdfModelHelper.arraysEquivalent(this.getMembers(), ((LicenseSet)compare).getMembers());
	}
}
