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

import java.util.List;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.base.Objects;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
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
	
	@Override 
	public List<String> verify() {
		List<String> retval = super.verify();
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
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.CLASS_SPDX_LICENSE);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {	
		return this.createStdLicenseUri(this.licenseId);
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
		if (this.licenseId == null) {
			return sCompare.getLicenseId() == null;
		} else if (sCompare.getLicenseId() == null) {
			return false;
		} else {
			return this.licenseId.equalsIgnoreCase(sCompare.getLicenseId());
		}
	}

}
