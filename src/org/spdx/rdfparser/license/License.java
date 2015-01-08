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

import org.apache.commons.lang3.StringEscapeUtils;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
	 * @param licenseNode RDF graph node representing the SPDX License
	 * @throws InvalidSPDXAnalysisException 
	 */
	public License(Model spdxModel, Node licenseNode) throws InvalidSPDXAnalysisException {
		super(spdxModel, licenseNode);
		// text
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT).asNode();
		Triple m = Triple.createMatch(licenseInfoNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseText = t.getObject().toString(false);
			if (this.licenseText.endsWith(XML_LITERAL)) {
				this.licenseText = this.licenseText.substring(0, this.licenseText.length()-XML_LITERAL.length());
			}
			if (this.textInHtml) {
				this.licenseText = SpdxLicenseTemplateHelper.HtmlToText(this.licenseText);
			}
		}
		// standardLicenseHeader
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.standardLicenseHeader = t.getObject().toString(false);
		} else {
			// try the 1.0 version name
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_VERSION_1).asNode();
			m = Triple.createMatch(licenseNode, p, null);
			tripleIter = model.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.standardLicenseHeader = t.getObject().toString(false);
			} else {
				this.standardLicenseHeader = null;
			}
		}
		if (this.standardLicenseHeader != null) {
			this.standardLicenseHeader = StringEscapeUtils.unescapeHtml4(this.standardLicenseHeader);
		}
		// template
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.standardLicenseTemplate = t.getObject().toString(false);
			if (standardLicenseTemplate.endsWith(XML_LITERAL)) {
				this.standardLicenseTemplate = this.standardLicenseTemplate.substring(0, this.standardLicenseTemplate.length()-XML_LITERAL.length());
			}
			if (this.templateInHtml) {
				this.standardLicenseTemplate = SpdxLicenseTemplateHelper.HtmlToText(this.standardLicenseTemplate);
			}
		} else {
			// try version 1
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1).asNode();
			m = Triple.createMatch(licenseNode, p, null);
			tripleIter = model.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.standardLicenseTemplate = t.getObject().toString(false);
				if (standardLicenseTemplate.endsWith(XML_LITERAL)) {
					this.standardLicenseTemplate = this.standardLicenseTemplate.substring(0, this.standardLicenseTemplate.length()-XML_LITERAL.length());
				}
				if (this.templateInHtml) {
					this.standardLicenseTemplate = SpdxLicenseTemplateHelper.HtmlToText(this.standardLicenseTemplate);
				}
			} else {
				this.standardLicenseTemplate = null;
			}
		}
		// OSI Approved
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (!tripleIter.hasNext()) {
			// for compatibility, check the version 1 property name
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED_VERSION_1).asNode();
			m = Triple.createMatch(licenseNode, p, null);
			tripleIter = model.getGraph().find(m);	
		}
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			String osiTextValue = t.getObject().toString(false);
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


	/**
	 * @return the text of the license
	 */
	public String getLicenseText() {
		return this.licenseText;
	}

	/**
	 * @param text the license text to set
	 */
	public void setLicenseText(String text) {
		this.licenseText = text;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT);
			resource.addProperty(p, text);
			this.textInHtml = false;
		}
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
		return standardLicenseHeader;
	}
	
	/**
	 * @param standardLicenseHeader the standardLicenseHeader to set
	 */
	public void setStandardLicenseHeader(String standardLicenseHeader) {
		this.standardLicenseHeader = standardLicenseHeader;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE);
			model.removeAll(resource, p, null);
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER_VERSION_1);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTICE);
			resource.addProperty(p, this.standardLicenseHeader);
		}
	}
	/**
	 * @return the template
	 */
	public String getStandardLicenseTemplate() {
		return standardLicenseTemplate;
	}
	/**
	 * @param template the template to set
	 */
	public void setStandardLicenseTemplate(String template) {
		this.standardLicenseTemplate = template;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1);
			model.removeAll(resource, p, null);
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			resource.addProperty(p, this.standardLicenseTemplate);
			this.templateInHtml = false;
		}
	}
	
	@Override
	public String toString() {
		// must be only the ID if we want to reuse the 
		// toString for creating parseable license info strings
		return this.licenseId;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.CLASS_SPDX_LICENSE);
		Resource r = super._createResource(model, type);
		//text
		if (this.licenseText != null) {
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
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
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
		return this.osiApproved;
	}
	
	public void setOsiApproved(boolean osiApproved) {
		this.osiApproved = osiApproved;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
			model.removeAll(resource, p, null);
			// also delete any of the version 1 property names
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED_VERSION_1);
			model.removeAll(resource, p, null);
			// add the property
			if (this.osiApproved) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
				resource.addProperty(p, String.valueOf(this.osiApproved));
			}
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
	
}
