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

import org.apache.commons.lang3.StringEscapeUtils;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Listed license for SPDX
 * @author Gary O'Neall
 *
 */
public class SPDXStandardLicense extends SPDXLicense {
	
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
	private String name;
	private String[] sourceUrl;
	private String comments;
	private String standardLicenseHeader;
	private String template;
	private String text;
	private boolean osiApproved;
	
//	Uncomment below for allow compatibility
	//	public SPDXStandardLicense(String name, String id, String text, String sourceUrl, String notes,
//			String standardLicenseHeader, String template, boolean osiApproved) throws InvalidSPDXAnalysisException {
//		this(name, id, text, new String[] {sourceUrl}, notes, standardLicenseHeader, template, osiApproved);
//	}
	
	public SPDXStandardLicense(String name, String id, String text, String[] sourceUrl, String comments,
			String standardLicenseHeader, String template, boolean osiApproved) throws InvalidSPDXAnalysisException {
		super(id);
// The following check was removed since this class is used in creating the standard licenses for the website
//		if (!SPDXLicenseInfoFactory.isStandardLicenseID(id)) {
//		    throw new InvalidSPDXAnalysisException("Invalid standard license ID: " + id);
//		}
		this.name = name;
		this.sourceUrl = sourceUrl;
		this.comments = comments;
		this.standardLicenseHeader = standardLicenseHeader;
		this.template = template;
		
		this.osiApproved = osiApproved;
		this.text = text;
	}
	/**
	 * Constructs an SPDX License from the licenseNode
	 * @param licenseNode RDF graph node representing the SPDX License
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXStandardLicense(Model spdxModel, Node licenseNode) throws InvalidSPDXAnalysisException {
		super(spdxModel, licenseNode);
		// name
		this.name = null;
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME).asNode();
		Triple m = Triple.createMatch(licenseNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		} else {
			// try the pre 1.1 name - for backwards compatibility
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME_VERSION_1).asNode();
			m = Triple.createMatch(licenseNode, p, null);
			tripleIter = model.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.name = t.getObject().toString(false);
			} else {
				this.name = id;	// No name hsa been found, default is the ID
			}
		}
		// text
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.text = t.getObject().toString(false);
			if (this.text.endsWith(XML_LITERAL)) {
				this.text = this.text.substring(0, this.text.length()-XML_LITERAL.length());
			}
			if (this.textInHtml) {
				this.text = SpdxLicenseTemplateHelper.HtmlToText(this.text);
			}
		}

		// SourceUrl/seeAlso
		ArrayList<String> alsourceUrls = new ArrayList<String>();
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alsourceUrls.add(t.getObject().toString(false));
		}
		// The following is added for compatibility with earlier versions
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alsourceUrls.add(t.getObject().toString(false));
		}
		p = model.getProperty(SpdxRdfConstants.OWL_NAMESPACE, SpdxRdfConstants.PROP_OWL_SAME_AS).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alsourceUrls.add(t.getObject().toString(false));
		}
		this.sourceUrl = alsourceUrls.toArray(new String[alsourceUrls.size()]);
		// notes
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (!tripleIter.hasNext()) {
			// check the old property name for compatibility with pre-1.1 generated RDF files
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1).asNode();
			m = Triple.createMatch(licenseNode, p, null);
			tripleIter = model.getGraph().find(m);	
		}
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.comments = t.getObject().toString(false);
		} else {
			this.comments = null;
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
			this.template = t.getObject().toString(false);
			if (template.endsWith(XML_LITERAL)) {
				this.template = this.template.substring(0, this.template.length()-XML_LITERAL.length());
			}
			if (this.templateInHtml) {
				this.template = SpdxLicenseTemplateHelper.HtmlToText(this.template);
			}
		} else {
			// try version 1
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1).asNode();
			m = Triple.createMatch(licenseNode, p, null);
			tripleIter = model.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.template = t.getObject().toString(false);
				if (template.endsWith(XML_LITERAL)) {
					this.template = this.template.substring(0, this.template.length()-XML_LITERAL.length());
				}
				if (this.templateInHtml) {
					this.template = SpdxLicenseTemplateHelper.HtmlToText(this.template);
				}
			} else {
				this.template = null;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			model.removeAll(resource, p, null);
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME_VERSION_1);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			resource.addProperty(p, this.name);
		}
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
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
	/**
	 * @return the sourceUrl
	 */
	public String[] getSourceUrl() {
		return sourceUrl;
	}
	/**
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setSourceUrl(String[] sourceUrl) {
		this.sourceUrl = sourceUrl;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			// the following is to fix any earlier versions using the old property name
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
			model.removeAll(resource, p, null);
			p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			model.removeAll(resource, p, null);
			// add the property
			p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			for (int i = 0; i < sourceUrl.length; i++) {
				resource.addProperty(p, this.sourceUrl[i]);
			}	
		}
	}
	/**
	 * @return the comments
	 */
	public String getComment() {
		return comments;
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
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comments = comment;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(resource, p, null);
			// Also delete any instances of the pre-1.1 property names
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			resource.addProperty(p, this.comments);
		}
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
	public String getTemplate() {
		return template;
	}
	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE_VERSION_1);
			model.removeAll(resource, p, null);
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			resource.addProperty(p, this.template);
			this.templateInHtml = false;
		}
	}
	
	@Override
	public String toString() {
		// must be only the ID if we want to reuse the 
		// toString for creating parseable license info strings
		return this.id;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.CLASS_SPDX_STANDARD_LICENSE);
		String uri = this.createStdLicenseUri(this.id);
		Resource r = super._createResource(model, type, uri);
		//text
		if (this.text != null) {
			Property textProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_LICENSE_TEXT);
			model.removeAll(r, textProperty, null);
			r.addProperty(textProperty, this.text);
		}
		//name
		if (name != null) {
			Property namePropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			r.addProperty(namePropery, this.name);
		}
		// comments
		if (this.comments != null) {
			Property notesPropery = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			r.addProperty(notesPropery, this.comments);
		}
		//source URL
		if (this.sourceUrl != null && this.sourceUrl.length > 0) {
			Property sourceUrlPropery = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			for (int i = 0; i < this.sourceUrl.length; i++) {
				r.addProperty(sourceUrlPropery, this.sourceUrl[i]);
			}
		}
		//standard license header
		if (this.standardLicenseHeader != null) {
			Property standardLicenseHeaderPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_NOTICE);
			r.addProperty(standardLicenseHeaderPropery, this.standardLicenseHeader);
		}
		//template
		if (this.template != null) {
			Property templatePropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			r.addProperty(templatePropery, this.template);
		}
		//Osi Approved
		if (this.osiApproved) {
			Property osiApprovedPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
			r.addProperty(osiApprovedPropery, String.valueOf(this.osiApproved));
		}
		return r;
	}
	
	/**
	 * Creates a standard license URI by appending the standard license ID to the URL hosting the SPDX licenses
	 * @param id Standard License ID
	 * @return
	 */
	private String createStdLicenseUri(String id) {
		return SpdxRdfConstants.STANDARD_LICENSE_URL + "/" + id;
	}
	
	@Override
	public int hashCode() {
		if (this.getId() != null) {
			return this.getId().hashCode();
		} else {
			return 0;
		}
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof SPDXStandardLicense)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		SPDXStandardLicense comp = (SPDXStandardLicense)o;
		if (this.id == null) {
			return (comp.getId() == null);
		} else {
			return (this.id.equals(comp.getId()));
		}
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		String id = this.getId();
		if (id == null || id.isEmpty()) {
			retval.add("Missing required license ID");
		}
		//Todo check to see if the id is a standard license id
		String name = this.getName();
		if (name == null || name.isEmpty()) {
			retval.add("Missing required license name");
		}
		this.getComment();
		this.getSourceUrl();
		this.getStandardLicenseHeader();
		this.getTemplate();
		String licenseText = this.getText();
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
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#clone()
	 */
	@Override
	public SPDXLicenseInfo clone() {
		try {
			return new SPDXStandardLicense(this.getName(), this.getId(),
					this.getText(), this.getSourceUrl(), this.getComment(),
					this.getStandardLicenseHeader(), this.getTemplate(), this.isOsiApproved());
		} catch (InvalidSPDXAnalysisException e) {
			// Hmmm - TODO: Figure out what to do in this case
			return null;
		}
	}
}
