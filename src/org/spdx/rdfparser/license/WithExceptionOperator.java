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
 * A license that has a With exception operator (e.g. GPL-2.0 WITH Autoconf-exception-2.0)
 * @author Gary O'Neall
 *
 */
public class WithExceptionOperator extends AnyLicenseInfo {
	
	private AnyLicenseInfo license;
	private LicenseException exception;
	
	/**
	 * Create a WithExceptionOperator from a node in an existing RDF model
	 * @param modelContainer contains the model
	 * @param licenseInfoNode Node which defines the WithExceptionOperator
	 * @throws InvalidSPDXAnalysisException 
	 */
	public WithExceptionOperator(IModelContainer modelContainer, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseInfoNode);
		getPropertiesFromModel();
	}


	public WithExceptionOperator(AnyLicenseInfo license, LicenseException exception) {
		super();
		this.license = license;
		this.exception = exception;
	}


	protected Resource _createResource(Resource type) throws InvalidSPDXAnalysisException {
		Resource r = model.createResource(type);
		Property licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
		Resource licResource = this.license.createResource(this.modelContainer);
		r.addProperty(licProperty, licResource);
		Property exceptionProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION);
		Resource exceptionResource = this.exception.createResource(modelContainer);
		r.addProperty(exceptionProperty, exceptionResource);
		return r;
	}
	
	/**
	 * @return the license
	 */
	public AnyLicenseInfo getLicense() {
		if (this.resource != null && this.refreshOnGet) {
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER).asNode();
			Triple m = Triple.createMatch(node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				AnyLicenseInfo anyLicense;
				try {
					anyLicense = LicenseInfoFactory.getLicenseInfoFromModel(this.modelContainer, t.getObject());
					if (!(anyLicense instanceof SimpleLicensingInfo) && !(anyLicense instanceof OrLaterOperator)) {
						logger.warn("The license for a WITH expression must be of type SimpleLicensingInfo or an OrLaterOperator, using stored exception license");
					}
					this.license = anyLicense;
				} catch (InvalidSPDXAnalysisException e) {
					logger.warn("Error getting license info - using stored value",e);
				}				
			}
		}
		return license;
	}


	/**
	 * @param license the license to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setLicense(AnyLicenseInfo license) throws InvalidSPDXAnalysisException {
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


	/**
	 * @return the exception
	 */
	public LicenseException getException() {
		if (this.resource != null && this.refreshOnGet) {
			Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION).asNode();
			Triple m = Triple.createMatch(node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				try {
					this.exception = new LicenseException(modelContainer, t.getObject());
				} catch (InvalidSPDXAnalysisException e) {
					logger.warn("Error getting exception, using stored exception value", e);
				}
			}
		}
		return exception;
	}


	/**
	 * @param exception the exception to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setException(LicenseException exception) throws InvalidSPDXAnalysisException {
		this.exception = exception;
		if (model != null && node != null) {
			// delete any previous created
			Property exceptionProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION);
			model.removeAll(resource, exceptionProperty, null);

			exceptionProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION);
			Resource exceptionResource = exception.createResource(modelContainer);
			resource.addProperty(exceptionProperty, exceptionResource);
		}
	}


	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		if (license == null || exception == null) {
			return "UNDEFINED WITH EXCEPTION";
		}
		return license.toString() +" "+ exception.toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof WithExceptionOperator)) {
			return false;
		}
		WithExceptionOperator comp = (WithExceptionOperator)o;
		if (comp.getLicense() == null) {
			if (this.getLicense() != null) {
				return false;
			}
		} else if (!comp.getLicense().equals(this.getLicense())) {
			return false;
		}
		if (comp.getException() == null) {
			if (this.getException() != null) {
				return false;
			}
		} else if (!comp.getException().equals(this.getException())) {
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
		int exceptionHashCode = 0;
		if (this.license != null) {
			licHashCode = this.license.hashCode();
		}
		if (this.exception != null) {
			exceptionHashCode = this.exception.hashCode();
		}
		return licHashCode ^ exceptionHashCode;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		if (this.license == null) {
			retval.add("Missing required license for a License WITH Exception");
		} else {
			retval.addAll(this.license.verify());
		}
		if (this.exception == null) {
			retval.add("Missing required exception for a License WITH Exception");
		} else {
			retval.addAll(this.exception.verify());
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#clone()
	 */
	@Override
	public AnyLicenseInfo clone() {
		AnyLicenseInfo clonedLicense = null;
		if (this.license != null) {
			clonedLicense = this.license.clone();
		}
		LicenseException clonedException = null;
		if (this.exception != null) {
			clonedException = this.exception.clone();
		}
		return new WithExceptionOperator(clonedLicense, clonedException);
	}


	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (compare == this) {
			return true;
		}
		if (!(compare instanceof WithExceptionOperator)) {
			return false;
		}
		WithExceptionOperator wCompare = (WithExceptionOperator)compare;
		return (RdfModelHelper.equivalentConsideringNull(license, wCompare.getLicense()) &&
				RdfModelHelper.equivalentConsideringNull(this.exception, wCompare.getException()));
	}


	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// License
		// delete any previous created
		Property licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
		model.removeAll(resource, licProperty, null);

		licProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_SET_MEMEBER);
		Resource licResource = license.createResource(this.modelContainer);
		resource.addProperty(licProperty, licResource);
		// Exception
		// delete any previous created
		Property exceptionProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION);
		model.removeAll(resource, exceptionProperty, null);

		exceptionProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION);
		Resource exceptionResource = exception.createResource(modelContainer);
		resource.addProperty(exceptionProperty, exceptionResource);
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
			AnyLicenseInfo anyLicense = LicenseInfoFactory.getLicenseInfoFromModel(this.modelContainer, t.getObject());
			if (!(anyLicense instanceof SimpleLicensingInfo) && !(anyLicense instanceof OrLaterOperator)) {
				throw (new InvalidSPDXAnalysisException("The license for a WITH expression must be of type SimpleLicensingInfo or an OrLaterOperator"));
			}
			this.license = anyLicense;
		}
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION).asNode();
		m = Triple.createMatch(node, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (this.exception != null) {
				throw (new InvalidSPDXAnalysisException("More than one exception license WITH expression"));
			}
			this.exception = new LicenseException(modelContainer, t.getObject());
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
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_WITH_EXCEPTION_OPERATOR);
	}

}
