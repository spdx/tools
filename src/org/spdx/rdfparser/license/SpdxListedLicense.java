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

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Listed license for SPDX as listed at spdx.org/licenses
 * @author Gary O'Neall
 *
 */
public class SpdxListedLicense extends License {
	

	public SpdxListedLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved) throws InvalidSPDXAnalysisException {
		super(name, id, text, sourceUrl, comments, standardLicenseHeader, template, osiApproved);
	}
	/**
	 * Constructs an SPDX License from the licenseNode
	 * @param modelContainer container which includes the license
	 * @param licenseNode RDF graph node representing the SPDX License
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxListedLicense(IModelContainer modelContainer, Node licenseNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseNode);
	}
	
	@Override public ArrayList<String> verify() {
		ArrayList<String> retval = super.verify();
		if (!LicenseInfoFactory.isSpdxListedLicenseID(this.getLicenseId())) {
			retval.add("License "+this.getLicenseId()+" is not a listed license at spdx.org/licenses");
		}
		return retval;
	}
	
	
	/**
	 * Creates a standard license URI by appending the standard license ID to the URL hosting the SPDX licenses
	 * @param id Standard License ID
	 * @return
	 */
	private String createStdLicenseUri(String id) {
		return SpdxRdfConstants.STANDARD_LICENSE_URL + "/" + id;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource() {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.CLASS_SPDX_LICENSE);
		String uri = this.createStdLicenseUri(this.licenseId);
		Resource r = super._createResource(type, uri);
		//text
		if (licenseText != null) {
			Property textProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_LICENSE_TEXT);
			model.removeAll(r, textProperty, null);
			r.addProperty(textProperty, this.licenseText);
		}
		//standard license header
		if (this.standardLicenseHeader != null) {
			Property standardLicenseHeaderPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_NOTICE);
			r.addProperty(standardLicenseHeaderPropery, this.standardLicenseHeader);
		}
		//template
		if (this.standardLicenseTemplate != null) {
			Property templatePropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			r.addProperty(templatePropery, this.standardLicenseTemplate);
		}
		//Osi Approved
		if (this.osiApproved) {
			Property osiApprovedPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
			r.addProperty(osiApprovedPropery, String.valueOf(this.osiApproved));
		}
		return r;
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof SpdxListedLicense)) {
			return false;
		}
		// For a listed license, if the ID's equal, it is considered equivalent
		SpdxListedLicense sCompare = (SpdxListedLicense)compare;
		return RdfModelHelper.equalsConsideringNull(this.licenseId, sCompare.getLicenseId());
	}

}
