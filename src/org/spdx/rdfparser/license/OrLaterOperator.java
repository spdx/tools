/**
 * Copyright (c) 2015 Source Auditor Inc.
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

import java.util.List;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A license that has an or later operator (e.g. GPL-2.0+)
 * @author Gary O'Neall
 *
 */
public class OrLaterOperator extends AnyLicenseInfo {

	private SimpleLicensingInfo license;
	/**
	 * Create an OrLaterOperator from a node in an existing RDF model
	 * @param enclosingSpdxDocument document which includes the license
	 * @param licenseInfoNode Node that defines the OrLaterOperator
	 * @throws InvalidSPDXAnalysisException 
	 */
	public OrLaterOperator(IModelContainer modelContainer, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseInfoNode);
		getPropertiesFromModel();
	}
	
	public OrLaterOperator(SimpleLicensingInfo license) {
		super();
		this.license = license;
	}
	
	/**
	 * @return the license
	 */
	public SimpleLicensingInfo getLicense() {
		if (this.resource != null && this.refreshOnGet) {
			try {
				this.populateModel();
			} catch (InvalidSPDXAnalysisException e) {
				logger.warn("Error getting license from model, using stored value", e);
			}
		}
		return license;
	}

	/**
	 * @param license the license to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setLicense(SimpleLicensingInfo license) throws InvalidSPDXAnalysisException {
		this.license = license;
		if (model != null && node != null) {
			// delete any previous created
			Property licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
			model.removeAll(resource, licProperty, null);

			licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
			Resource licResource = license.createResource(this.modelContainer);
			resource.addProperty(licProperty, licResource);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		if (license == null) {
			return "UNDEFINED OR EXCEPTION";
		}
		return license.toString() + "+";
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof OrLaterOperator)) {
			return false;
		}
		OrLaterOperator comp = (OrLaterOperator)o;
		if (comp.getLicense() == null) {
			if (this.getLicense() != null) {
				return false;
			}
		} else if (!comp.getLicense().equals(this.getLicense())) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#hashCode()
	 */
	@Override
	public int hashCode() {
		int licHashCode = 0;
		if (this.license != null) {
			licHashCode = this.license.hashCode();
		}
		return licHashCode;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		if (this.license == null) {
			retval.add("Missing required license for a License Or Later operator");
		} else {
			retval.addAll(this.license.verify());
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#clone()
	 */
	@Override
	public AnyLicenseInfo clone() {
		SimpleLicensingInfo clonedLicense = null;
		if (this.license != null) {
			clonedLicense = (SimpleLicensingInfo)this.license.clone();
		}
		return new OrLaterOperator(clonedLicense);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof OrLaterOperator)) {
			return false;
		}
		return RdfModelHelper.equivalentConsideringNull(this.getLicense(), ((OrLaterOperator)compare).getLicense());
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
		Resource licResource = license.createResource(this.modelContainer);
		resource.addProperty(licProperty, licResource);		
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (this.license != null) {
				throw (new InvalidSPDXAnalysisException("More than one license for a license WITH expression"));
			}
			AnyLicenseInfo anyLicense = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, t.getObject());
			if (!(anyLicense instanceof SimpleLicensingInfo)) {
				throw (new InvalidSPDXAnalysisException("The license for a WITH expression must be of type SimpleLicensingInfo"));
			}
			this.license = (SimpleLicensingInfo)anyLicense;
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		// Use anonymous node
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_OR_LATER_OPERATOR);
	}
}