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
import java.util.HashSet;
import java.util.Iterator;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A specific form of license information where there is a set of licenses
 * represented
 * @author Gary O'Neall
 *
 */
public abstract class SPDXLicenseSet extends SPDXLicenseInfo {

	protected Set<SPDXLicenseInfo> licenseInfos = Sets.newHashSet();

	/**
	 * @param model
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException
	 */
	public SPDXLicenseSet(Model model, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(model, licenseInfoNode);
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER).asNode();
		Triple m = Triple.createMatch(licenseInfoNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseInfos.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
		}
	}


	public SPDXLicenseSet(SPDXLicenseInfo[] licenseInfos) {
		super();
		if (licenseInfos != null) {
			for (int i = 0; i < licenseInfos.length; i++) {
				this.licenseInfos.add(licenseInfos[i]);
			}
		}
	}


	protected Resource _createResource(Model model, Resource type) {
		Resource r = model.createResource(type);
		Property licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
		Iterator<SPDXLicenseInfo> iter = this.licenseInfos.iterator();
		while (iter.hasNext()) {
			Resource licResource = iter.next().createResource(model);
			r.addProperty(licProperty, licResource);
		}
		return r;
	}

	public void setSPDXLicenseInfos(SPDXLicenseInfo[] licenseInfos) {
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
				Resource licResource = licenseInfos[i].createResource(model);
				resource.addProperty(licProperty, licResource);
			}
		}
	}

	public SPDXLicenseInfo[] getSPDXLicenseInfos() {
		SPDXLicenseInfo[] retval = new SPDXLicenseInfo[this.licenseInfos.size()];
		retval = this.licenseInfos.toArray(retval);
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		Iterator<SPDXLicenseInfo> iter = licenseInfos.iterator();
		while (iter.hasNext()) {
			retval.addAll(iter.next().verify());
		}
		return retval;
	}
}
