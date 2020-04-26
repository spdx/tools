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
import org.apache.jena.graph.Node;

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
	private boolean templateInHtml = false;
	protected String standardLicenseHeader;
	protected String standardLicenseTemplate;
	protected String licenseText;
	protected boolean osiApproved;
	protected Boolean fsfLibre = null;
	protected boolean deprecated;

	private String standardLicenseHeaderTemplate;
		
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
		this(name,id,text,sourceUrl,comments,standardLicenseHeader,template,osiApproved,null);
	}
	
	/**
	 * @param name License name
	 * @param id License ID
	 * @param text License text
	 * @param sourceUrl Optional URLs that reference this license
	 * @param comments Optional comments
	 * @param standardLicenseHeader Optional license header
	 * @param template Optional template
	 * @param standardLicenseHeaderTemplate optional template for the standard license header
	 * @param osiApproved True if this is an OSI Approvied license
	 * @param fsfLibre true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre, null if FSF does not reference the license
	 * @throws InvalidSPDXAnalysisException
	 */
	public License(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, String standardLicenseHeaderTemplate,
			boolean osiApproved, Boolean fsfLibre) throws InvalidSPDXAnalysisException {
		super(name, id, comments, sourceUrl);
		this.standardLicenseHeader = standardLicenseHeader;
		this.standardLicenseTemplate = template;
		
		this.osiApproved = osiApproved;
		this.fsfLibre = fsfLibre;
		this.licenseText = text;
		this.deprecated = false;
		this.standardLicenseHeaderTemplate = standardLicenseHeaderTemplate;
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
	public License(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved, Boolean fsfLibre) throws InvalidSPDXAnalysisException {
		this(name, id, text, sourceUrl, comments, standardLicenseHeader, template, standardLicenseHeader, 
				osiApproved, fsfLibre);
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
		standardLicenseHeaderTemplate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_TEMPLATE);
		if (standardLicenseHeaderTemplate != null) {
			standardLicenseHeaderTemplate = StringEscapeUtils.unescapeHtml4(standardLicenseHeaderTemplate);
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
		String fsfTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE);
		if (fsfTextValue != null) {
			fsfTextValue = fsfTextValue.trim();
			if (fsfTextValue.equals("true") || fsfTextValue.equals("1")) {
				this.fsfLibre = true;
			} else if (fsfTextValue.equals("false") || fsfTextValue.equals("0")) {
				this.fsfLibre = false;
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid value for FSF Free - must be {true, false, 0, 1}"));
			}
		} else {
			fsfLibre = null;
		}
		// Deprecated
		String deprecatedValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_ID_DEPRECATED);
		if (deprecatedValue != null) {
			deprecatedValue = deprecatedValue.trim();
			if (deprecatedValue.equals("true") || deprecatedValue.equals("1")) {
				this.deprecated = true;
			} else if (deprecatedValue.equals("false") || deprecatedValue.equals("0")) {
				this.deprecated = false;
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid value for license deprecated - must be {true, false, 0, 1}"));
			}
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
		// FSF Libre
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE);
		if (this.fsfLibre != null) {
			if (this.fsfLibre) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE, "true");
			} else {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE, "false");
			}
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE);
		}
		// Headers
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_VERSION_1);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE, standardLicenseHeader);
		// header template
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_TEMPLATE);
		if (this.standardLicenseHeaderTemplate != null) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_TEMPLATE, standardLicenseHeaderTemplate);
		}
		// Template
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE, this.standardLicenseTemplate);
		this.templateInHtml = false;	// stored in the clear
		if (this.deprecated) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_ID_DEPRECATED, "true");
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_ID_DEPRECATED);
		}
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
	 * @return standard license header template
	 */
	public String getStandardLicenseHeaderTemplate() {
		if (this.resource != null && this.refreshOnGet) {
			standardLicenseHeaderTemplate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_TEMPLATE);
			if (standardLicenseHeaderTemplate != null) {
				standardLicenseHeaderTemplate = StringEscapeUtils.unescapeHtml4(standardLicenseHeaderTemplate);
			}
		}
		return standardLicenseHeaderTemplate;
	}
	
	/**
	 * @param standardLicenseHeaderTemplate
	 */
	public void setStandardLicenseHeaderTemplate(String standardLicenseHeaderTemplate) {
		this.standardLicenseHeaderTemplate = standardLicenseHeaderTemplate;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_TEMPLATE);
		if (standardLicenseHeaderTemplate != null) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_TEMPLATE, standardLicenseHeaderTemplate);
		}
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
		if (this.licenseId == null) {
			return "NULL LICENSE";
		} else {
			return this.licenseId;
		}
	}

	@Override
	public int hashCode() {
		if (this.getLicenseId() != null) {
			return this.getLicenseId().toLowerCase().hashCode();
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
		if (!(o instanceof License)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		License comp = (License)o;
		if (this.licenseId == null) {
			return (comp.getLicenseId() == null);
		} else {
			return (this.licenseId.equalsIgnoreCase(comp.getLicenseId()));
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
		//TODO Add test for template
		this.getStandardLicenseHeaderTemplate();
		//TODO add test for license header template
		String licenseText = this.getLicenseText();
		if (licenseText == null || licenseText.isEmpty()) {
			retval.add("Missing required license text for " + id);
		}
		return retval;
	}
	
	/**
	 * @return true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre or if FSF does not reference the license
	 * @throws InvalidSPDXAnalysisException
	 */
	public boolean isFsfLibre() {
		if (this.resource != null && this.refreshOnGet) {
			String fsfTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE);
			if (fsfTextValue != null) {
				fsfTextValue = fsfTextValue.trim();
				if (fsfTextValue.equals("true") || fsfTextValue.equals("1")) {
					this.fsfLibre = true;
				} else if (fsfTextValue.equals("false") || fsfTextValue.equals("0")) {
					this.fsfLibre = false;
				}
			} else {
				fsfLibre = null;
			}
		}
		return this.fsfLibre != null && this.fsfLibre;
	}
	
	/**
	 * @return true if FSF specified this license as not free/libre, false if it has been specified by the FSF as free / libre or if it has not been specified
	 */
	public boolean isNotFsfLibre() {
		if (this.resource != null && this.refreshOnGet) {
			String fsfTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE);
			if (fsfTextValue != null) {
				fsfTextValue = fsfTextValue.trim();
				if (fsfTextValue.equals("true") || fsfTextValue.equals("1")) {
					this.fsfLibre = true;
				} else if (fsfTextValue.equals("false") || fsfTextValue.equals("0")) {
					this.fsfLibre = false;
				}
			} else {
				fsfLibre = null;
			}
		}
		return this.fsfLibre != null && !this.fsfLibre;
	}
	
	/**
	 * @return true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre, null if FSF does not reference the license
	 */
	public Boolean getFsfLibre() {
		if (this.resource != null && this.refreshOnGet) {
			String fsfTextValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE);
			if (fsfTextValue != null) {
				fsfTextValue = fsfTextValue.trim();
				if (fsfTextValue.equals("true") || fsfTextValue.equals("1")) {
					this.fsfLibre = true;
				} else if (fsfTextValue.equals("false") || fsfTextValue.equals("0")) {
					this.fsfLibre = false;
				}
			} else {
				fsfLibre = null;
			}
		}
		return this.fsfLibre;
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
	
	/**
	 * @return true if this license is marked as being deprecated
	 */
	public boolean isDeprecated() {
		if (this.resource != null && this.refreshOnGet) {
			String deprecatedValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_ID_DEPRECATED);
			if (deprecatedValue != null) {
				deprecatedValue = deprecatedValue.trim();
				if (deprecatedValue.equals("true") || deprecatedValue.equals("1")) {
					this.deprecated = true;
				} else if (deprecatedValue.equals("false") || deprecatedValue.equals("0")){
					this.deprecated = false;
				} else {
					this.deprecated = false;
				}
			} else {			
				this.deprecated = false;
			}
		}
		return this.deprecated;
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
	
	/**
	 * @param fsfLibre true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre, null if FSF does not reference the license
	 */
	public void setFsfLibre(Boolean fsfLibre) {
		this.fsfLibre = fsfLibre;
		if (fsfLibre != null) {
			if (fsfLibre) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE, "true");
			} else {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE, "false");
			}
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_FSF_LIBRE);
		}
	}
	
	/**
	 * @param deprecated true if this license is deprecated
	 */
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_ID_DEPRECATED);
		
		if (this.deprecated) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_ID_DEPRECATED, "true");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#clone()
	 */
	@Override
	public AnyLicenseInfo clone() {
		try {
			SpdxListedLicense retval = new SpdxListedLicense(this.getName(), this.getLicenseId(),
					this.getLicenseText(), this.getSeeAlso(), this.getComment(),
					this.getStandardLicenseHeader(), this.getStandardLicenseTemplate(), 
					this.getStandardLicenseHeaderTemplate(), this.isOsiApproved(), this.getFsfLibre());
			retval.setDeprecated(this.isDeprecated());
			return retval;
		} catch (InvalidSPDXAnalysisException e) {
			throw new AssertionError("Clone should never cause an Invalid SPDX Exception",e);
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
		this.setStandardLicenseHeaderTemplate(license.getStandardLicenseHeaderTemplate());
		this.setFsfLibre(license.getFsfLibre());
		this.setDeprecated(license.isDeprecated());
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
