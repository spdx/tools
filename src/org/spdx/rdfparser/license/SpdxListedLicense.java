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

import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * Listed license for SPDX as listed at spdx.org/licenses
 * @author Gary O'Neall
 *
 */
public class SpdxListedLicense extends License {
	
	private String licenseTextHtml = null;
	private String licenseHeaderHtml = null;
	private String deprecatedVersion = null;
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param text License text
	 * @param sourceUrl Optional URLs that reference this license
	 * @param comments Optional comments
	 * @param standardLicenseHeader Optional license header
	 * @param template Optional template
	 * @param osiApproved True if this is an OSI Approved license
	 * @param fsfLibre true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre, null if FSF does not reference the license
	 * @param licenseTextHtml HTML version for the license text
	 * @param isDeprecated True if this license has been designated as deprecated by the SPDX legal team
	 * @param deprecatedVersion License list version when this license was first deprecated (null if not deprecated)
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved, Boolean fsfLibre, 
			String licenseTextHtml, boolean isDeprecated, String deprecatedVersion) throws InvalidSPDXAnalysisException {
		super(name, id, text, sourceUrl, comments, standardLicenseHeader, template, osiApproved, fsfLibre);
		this.licenseTextHtml = licenseTextHtml;
		this.setDeprecated(isDeprecated);
		this.deprecatedVersion = deprecatedVersion;
	}
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param text License text
	 * @param sourceUrl Optional URLs that reference this license
	 * @param comments Optional comments
	 * @param standardLicenseHeader Optional license header
	 * @param template Optional template
	 * @param osiApproved True if this is an OSI Approved license
	 * @param licenseTextHtml HTML version for the license text
	 * @throws InvalidSPDXAnalysisException
	 */	
	public SpdxListedLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved, String licenseTextHtml) throws InvalidSPDXAnalysisException {
		this(name, id, text, sourceUrl, comments, standardLicenseHeader, template, osiApproved, false, licenseTextHtml, false, null);
	}
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param text License text
	 * @param sourceUrl Optional URLs that reference this license
	 * @param comments Optional comments
	 * @param standardLicenseHeader Optional license header
	 * @param template Optional template
	 * @param osiApproved True if this is an OSI Approvied license
	 * @param fsfLibre True if the license is listed by the Free Software Foundation as free / libre
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved, boolean fsfLibre) throws InvalidSPDXAnalysisException {
		this(name, id, text, sourceUrl, comments, standardLicenseHeader, template, osiApproved, fsfLibre, null, false, null);
	}
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param text License text
	 * @param sourceUrl Optional URLs that reference this license
	 * @param comments Optional comments
	 * @param standardLicenseHeader Optional license header
	 * @param template Optional template
	 * @param osiApproved True if this is an OSI Approvied license
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved) throws InvalidSPDXAnalysisException {
		this(name, id, text, sourceUrl, comments, standardLicenseHeader, template, osiApproved, false, null, false, null);
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
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param text License text
	 * @param sourceUrl Optional URLs that reference this license
	 * @param comments Optional comments
	 * @param standardLicenseHeader Optional license header
	 * @param template Optional template
	 * @param licenseHeaderTemplate optional template for the standard license header
	 * @param osiApproved True if this is an OSI Approved license
	 * @param fsfLibre True if the license is listed by the Free Software Foundation as free / libre
	 * @param licenseTextHtml HTML version for the license text
	 * @param licenseHeaderHtml HTML version for the standard license header
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, String licenseHeaderTemplate, boolean osiApproved, boolean fsfLibre, String licenseTextHtml,
			String licenseHeaderHtml) throws InvalidSPDXAnalysisException {
		
		super(name, id, text, sourceUrl, comments, standardLicenseHeader, template, licenseHeaderTemplate, osiApproved, fsfLibre);
		this.licenseTextHtml = licenseTextHtml;
		this.licenseHeaderHtml = licenseHeaderHtml;
	}
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param text License text
	 * @param sourceUrl Optional URLs that reference this license
	 * @param comments Optional comments
	 * @param standardLicenseHeader Optional license header
	 * @param template Optional template
	 * @param licenseHeaderTemplate optional template for the standard license header
	 * @param osiApproved True if this is an OSI Approved license
	 * @param fsfLibre True if the license is listed by the Free Software Foundation as free / libre
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxListedLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, String licenseHeaderTemplate, boolean osiApproved, Boolean fsfLibre) throws InvalidSPDXAnalysisException {
		
		super(name, id, text, sourceUrl, comments, standardLicenseHeader, template, licenseHeaderTemplate, osiApproved, fsfLibre);
	}

	@Override 
	public List<String> verify() {
		List<String> retval = super.verify();
		if (!LicenseInfoFactory.isSpdxListedLicenseID(this.getLicenseId())) {
			retval.add("License "+this.getLicenseId()+" is not a listed license at spdx.org/licenses");
		}
		if (this.isDeprecated()) {
			retval.add(this.licenseId + " is deprecated.");
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
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
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
	
	/**
	 * @return HTML fragment containing the License Text
	 * @throws InvalidLicenseTemplateException 
	 */
	public String getLicenseTextHtml() throws InvalidLicenseTemplateException {
		if (licenseTextHtml == null) {
			// Format the HTML using the text and template
			String templateText = this.getStandardLicenseTemplate();
			if (templateText != null && !templateText.trim().isEmpty()) {
				try {
					licenseTextHtml = SpdxLicenseTemplateHelper.templateTextToHtml(templateText);
				} catch(LicenseTemplateRuleException ex) {
					throw new InvalidLicenseTemplateException("Invalid license expression found in license text for license "+getName()+":"+ex.getMessage());
				}
			} else {
				licenseTextHtml = SpdxLicenseTemplateHelper.formatEscapeHTML(this.getLicenseText());
			}
		}
		return licenseTextHtml;
	}
	
	/**
	 * Set the licenseTextHtml
	 * @param licenseTextHtml HTML fragment representing the license text
	 */
	public void setLicenseTextHtml(String licenseTextHtml) {
		this.licenseTextHtml = licenseTextHtml;
	}
	
	/**
	 * @return HTML fragment containing the License standard header text
	 * @throws InvalidLicenseTemplateException 
	 */
	public String getLicenseHeaderHtml() throws InvalidLicenseTemplateException {
		if (licenseHeaderHtml == null) {
			// Format the HTML using the text and template
			String templateText = this.getStandardLicenseHeaderTemplate();
			if (templateText != null && !templateText.trim().isEmpty()) {
				try {
					licenseHeaderHtml = SpdxLicenseTemplateHelper.templateTextToHtml(templateText);
				} catch(LicenseTemplateRuleException ex) {
					throw new InvalidLicenseTemplateException("Invalid license expression found in standard license header for license "+getName()+":"+ex.getMessage());
				}
			} else if (this.getStandardLicenseHeader() == null) {
				licenseHeaderHtml = "";
			} else {
				licenseHeaderHtml = SpdxLicenseTemplateHelper.formatEscapeHTML(this.getStandardLicenseHeader());
			}
		}
		return licenseHeaderHtml;
	}
	
	/**
	 * Set the licenseHeaderTemplateHtml
	 * @param licenseHeaderHtml HTML fragment representing the license standard header text
	 */
	public void setLicenseHeaderHtml(String licenseHeaderHtml) {
		this.licenseHeaderHtml = licenseHeaderHtml;
	}
	
	/**
	 * @return the deprecatedVersion
	 */
	public String getDeprecatedVersion() {
		return deprecatedVersion;
	}

	/**
	 * @param deprecatedVersion the deprecatedVersion to set
	 */
	public void setDeprecatedVersion(String deprecatedVersion) {
		this.deprecatedVersion = deprecatedVersion;
	}

}
