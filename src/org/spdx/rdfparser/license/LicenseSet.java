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


import java.util.Set;
import java.util.Iterator;
import java.util.List;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.collect.Sets;
import com.google.common.collect.Lists;

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
		
	protected Set<AnyLicenseInfo> licenseInfos = Sets.newHashSet();

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
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		this.licenseInfos.clear();
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER).asNode();
		Triple m = Triple.createMatch(node, p, null);
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
		if (model != null && node != null) {
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
		if (this.resource != null && this.refreshOnGet) {
			try {
				getPropertiesFromModel();
			} catch (InvalidSPDXAnalysisException e) {
				logger.warn("Error getting properites from model, using stored values.",e);
			}
		}
		AnyLicenseInfo[] retval = new AnyLicenseInfo[this.licenseInfos.size()];
		retval = this.licenseInfos.toArray(retval);
		return retval;
	}	
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
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
		if (compare == this) {
			return true;
		}
		if (!(compare instanceof LicenseSet)) {
			return false;
		}
		return RdfModelHelper.arraysEquivalent(this.getMembers(), ((LicenseSet)compare).getMembers());
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// delete any previous created
		Property licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
		model.removeAll(resource, licProperty, null);

		licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
		for (AnyLicenseInfo licenseInfo:this.licenseInfos) {
			Resource licResource = licenseInfo.createResource(this.modelContainer);
			resource.addProperty(licProperty, licResource);
		}
	}
}
