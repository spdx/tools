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

import org.apache.commons.lang3.StringEscapeUtils;
import org.spdx.compare.LicenseCompareHelper;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;

/**
 * Describes a license
 * 
 * All licenses have an ID.  
 * Subclasses should extend this class to add additional properties.
 * 
 * @author Gary O'Neall
 *
 */
public abstract class License extends SimpleLicensingInfo {

	static final String XML_LITERAL = "^^http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

	/**
	 * True if the text in the RDF model uses HTML tags.  If this flag is true, the text will
	 * be converted on import from the model.
	 */
	private boolean textInHtml = true;
	/**
	 * True if the template in the RDF model uses HTML tags.  If this flag is true, the text will
	 * be converted on import from the model.
	 */
	private boolean templateInHtml = true;
	protected String standardLicenseHeader;
	protected String standardLicenseTemplate;
	protected String licenseText;
	protected boolean osiApproved;
		
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
	public License(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved) throws InvalidSPDXAnalysisException {
		super(name, id, comments, sourceUrl);
		this.standardLicenseHeader = standardLicenseHeader;
		this.standardLicenseTemplate = template;
		
		this.osiApproved = osiApproved;
		this.licenseText = text;
	}
	/**
	 * Constructs an SPDX License from the licenseNode
	 * @param modelContainer container which includes the license
	 * @param licenseNode RDF graph node representing the SPDX License
	 * @throws InvalidSPDXAnalysisException 
	 */
	public License(IModelContainer modelContainer, Node licenseNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseNode);
		getPropertiesFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		super.getPropertiesFromModel();
		// text
		licenseText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT);		
		if (licenseText != null && licenseText.endsWith(XML_LITERAL)) {
			this.licenseText = this.licenseText.substring(0, this.licenseText.length()-XML_LITERAL.length());
		}
		if (licenseText != null && this.textInHtml) {
			this.licenseText = SpdxLicenseTemplateHelper.htmlToText(this.licenseText);
		}
		// standardLicenseHeader
		standardLicenseHeader = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE);
		if (standardLicenseHeader == null) {
			standardLicenseHeader = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_VERSION_1);
		}
		if (standardLicenseHeader != null) {
			standardLicenseHeader = StringEscapeUtils.unescapeHtml4(standardLicenseHeader);
		}
		// template
		this.standardLicenseTemplate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
		if (this.standardLicenseTemplate == null) {
			// try version 1
			this.standardLicenseTemplate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1);
		} 
		if (standardLicenseTemplate != null && standardLicenseTemplate.endsWith(XML_LITERAL)) {
			this.standardLicenseTemplate = this.standardLicenseTemplate.substring(0, this.standardLicenseTemplate.length()-XML_LITERAL.length());
		}
		if (standardLicenseTemplate != null && this.templateInHtml) {
			this.standardLicenseTemplate = SpdxLicenseTemplateHelper.htmlToText(this.standardLicenseTemplate);
		}
		// OSI Approved
		String osiTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
		if (osiTextValue == null) {
			// for compatibility, check the version 1 property name
			osiTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED_VERSION_1);
		}
		if (osiTextValue != null) {
			osiTextValue = osiTextValue.trim();
			if (osiTextValue.equals("true") || osiTextValue.equals("1")) {
				this.osiApproved = true;
			} else if (osiTextValue.equals("false") || osiTextValue.equals("0")){
				this.osiApproved = false;
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid value for OSI Approved - must be {true, false, 0, 1}"));
			}
		} else {			
			this.osiApproved = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		// LicenseText
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT, this.licenseText);
		this.textInHtml = false;	// we stored it in the clear
		// OSI Approved
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED_VERSION_1);
		if (osiApproved) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED, "true");
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
		}
		// Headers
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_VERSION_1);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE, standardLicenseHeader);
		// Template
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE, this.standardLicenseTemplate);
		this.templateInHtml = false;	// stored in the clear
	}


	/**
	 * @return the text of the license
	 */
	public String getLicenseText() {
		if (this.resource != null && this.refreshOnGet) {
			this.licenseText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT);		
			if (this.licenseText != null && this.licenseText.endsWith(XML_LITERAL)) {
				this.licenseText = this.licenseText.substring(0, this.licenseText.length()-XML_LITERAL.length());
			}
			if (this.licenseText != null && this.textInHtml) {
				this.licenseText = SpdxLicenseTemplateHelper.htmlToText(this.licenseText);
			}
		}
		return this.licenseText;
	}

	/**
	 * @param text the license text to set
	 */
	public void setLicenseText(String text) {
		this.licenseText = text;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT, text);
		this.textInHtml = false;	// stored in the clear
	}

	@Deprecated
	/**
	 * Replaced by <code>getComment()</code>
	 * @return comments
	 */
	public String getNotes() {
		return getComment();
	}
	@Deprecated
	/**
	 * Replaced by <code>setComment(String comment)</code>
	 * @param notes Comment to set
	 */
	public void setNotes(String notes) {
		setComment(notes);
	}
	
	/**
	 * @return the standardLicenseHeader
	 */
	public String getStandardLicenseHeader() {
		if (this.resource != null && this.refreshOnGet) {
			this.standardLicenseHeader = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE);
			if (this.standardLicenseHeader == null) {
				this.standardLicenseHeader = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_VERSION_1);
			}
			if (this.standardLicenseHeader != null) {
				this.standardLicenseHeader = StringEscapeUtils.unescapeHtml4(this.standardLicenseHeader);
			}
		}
		return standardLicenseHeader;
	}
	
	/**
	 * @param standardLicenseHeader the standardLicenseHeader to set
	 */
	public void setStandardLicenseHeader(String standardLicenseHeader) {
		this.standardLicenseHeader = standardLicenseHeader;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_VERSION_1);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE, standardLicenseHeader);
	}
	/**
	 * @return the template
	 */
	public String getStandardLicenseTemplate() {
		if (this.resource != null && this.refreshOnGet) {
			this.standardLicenseTemplate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			if (this.standardLicenseTemplate == null) {
				// try version 1
				this.standardLicenseTemplate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1);
			} 
			if (standardLicenseTemplate != null && standardLicenseTemplate.endsWith(XML_LITERAL)) {
				this.standardLicenseTemplate = this.standardLicenseTemplate.substring(0, this.standardLicenseTemplate.length()-XML_LITERAL.length());
			}
			if (standardLicenseTemplate != null && this.templateInHtml) {
				this.standardLicenseTemplate = SpdxLicenseTemplateHelper.htmlToText(this.standardLicenseTemplate);
			}
		}
		return standardLicenseTemplate;
	}
	/**
	 * @param template the template to set
	 */
	public void setStandardLicenseTemplate(String template) {
		this.standardLicenseTemplate = template;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE, template);
		this.templateInHtml = false;	// stored in the clear
	}
	
	@Override
	public String toString() {
		// must be only the ID if we want to reuse the 
		// toString for creating parseable license info strings
		return this.licenseId;
	}

	@Override
	public int hashCode() {
		if (this.getLicenseId() != null) {
			return this.getLicenseId().hashCode();
		} else {
			return 0;
		}
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof SpdxListedLicense)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		SpdxListedLicense comp = (SpdxListedLicense)o;
		if (this.licenseId == null) {
			return (comp.getLicenseId() == null);
		} else {
			return (this.licenseId.equals(comp.getLicenseId()));
		}
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		String id = this.getLicenseId();
		if (id == null || id.isEmpty()) {
			retval.add("Missing required license ID");
		}
		String name = this.getName();
		if (name == null || name.isEmpty()) {
			retval.add("Missing required license name");
		}
		this.getComment();
		this.getSeeAlso();
		this.getStandardLicenseHeader();
		this.getStandardLicenseTemplate();
		String licenseText = this.getLicenseText();
		if (licenseText == null || licenseText.isEmpty()) {
			retval.add("Missing required license text for " + id);
		}
		return retval;
	}
	/**
	 * @return true if the license is listed as an approved license on the OSI website
	 */
	public boolean isOsiApproved() {
		if (this.resource != null && this.refreshOnGet) {
			String osiTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
			if (osiTextValue == null) {
				// for compatibility, check the version 1 property name
				osiTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED_VERSION_1);
			}
			if (osiTextValue != null) {
				osiTextValue = osiTextValue.trim();
				if (osiTextValue.equals("true") || osiTextValue.equals("1")) {
					this.osiApproved = true;
				} else if (osiTextValue.equals("false") || osiTextValue.equals("0")){
					this.osiApproved = false;
				} else {
					this.osiApproved = false;
				}
			} else {			
				this.osiApproved = false;
			}
		}
		return this.osiApproved;
	}
	
	public void setOsiApproved(boolean osiApproved) {
		this.osiApproved = osiApproved;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED_VERSION_1);
		if (osiApproved) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED, "true");
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
		}
		
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#clone()
	 */
	@Override
	public AnyLicenseInfo clone() {
		try {
			return new SpdxListedLicense(this.getName(), this.getLicenseId(),
					this.getLicenseText(), this.getSeeAlso(), this.getComment(),
					this.getStandardLicenseHeader(), this.getStandardLicenseTemplate(), this.isOsiApproved());
		} catch (InvalidSPDXAnalysisException e) {
			// Hmmm - TODO: Figure out what to do in this case
			return null;
		}
	}
	
	/**
	 * Copy all of the parameters from another license
	 * @param license
	 */
	public void copyFrom(License license) {
		this.setComment(license.getComment());
		this.setLicenseId(license.getLicenseId());
		this.setLicenseText(license.getLicenseText());
		this.setName(license.getName());
		this.setOsiApproved(license.isOsiApproved());
		this.setSeeAlso(license.getSeeAlso());
		this.setStandardLicenseHeader(license.getStandardLicenseHeader());
		this.setStandardLicenseTemplate(this.getStandardLicenseTemplate());
	}
	/**
	 * @param compare
	 * @return
	 */
	@Override
    public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof License)) {
			return false;
		}
		// only test the text - other fields do not apply - if the license text is equivalent, then the license is considered equivalent
		License lCompare = (License)compare;
		return LicenseCompareHelper.isLicenseTextEquivalent(this.licenseText, lCompare.getLicenseText());
				
	}
	
}
